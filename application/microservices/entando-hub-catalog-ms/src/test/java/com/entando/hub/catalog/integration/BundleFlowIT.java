package com.entando.hub.catalog.integration;

import static com.entando.hub.catalog.config.AuthoritiesConstants.ADMIN;
import static com.entando.hub.catalog.config.AuthoritiesConstants.MANAGER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.entando.hub.catalog.response.BundleGroupVersionFilteredResponseView;
import com.entando.hub.catalog.rest.BundleController.Bundle;
import com.entando.hub.catalog.testhelper.AssertionHelper;
import com.entando.hub.catalog.testhelper.TestHelper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.StatusResultMatchers;

@SpringBootTest
class BundleFlowIT extends BaseFlowIT {

    private static final String BASE_URL = "/api/bundles";

    @BeforeEach
    public void setUp() {
        super.setUpBundleFlowData();
    }

    @Test
    @WithMockUser(roles = {ADMIN})
    void anAdminShouldBeAbleToAccessEveryRequestedBundles() throws Exception {

        // given I am an admin
        when(securityHelperService.isUserAuthenticated()).thenReturn(true);
        when(securityHelperService.isAdmin()).thenReturn(true);

        // without filters should get everything
        List<Bundle> expectedList = List.of(
                TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)),
                TestHelper.stubBundleDto(bundle2.getId(), List.of(bundleGroupVersion2)),
                TestHelper.stubBundleDto(bundle3.getId(), List.of(bundleGroupVersion3)),
                TestHelper.stubBundleDto(bundle4.getId(), List.of(bundleGroupVersion4)));
        ResultActions resultActions = executeOkGetBundlesRequest(null, null);
        AssertionHelper.assertOnBundles(resultActions, expectedList);

        // filter by bundleGroupVersionId and CatalogId
        expectedList = List.of(TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)));
        resultActions = executeOkGetBundlesRequest(bundleGroupVersion1.getId(), catalog1.getId());
        AssertionHelper.assertOnBundles(resultActions, expectedList);

        // filter by bundleGroupVersionId
        expectedList = List.of(TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)));
        resultActions = executeOkGetBundlesRequest(bundleGroupVersion1.getId(), null);
        AssertionHelper.assertOnBundles(resultActions, expectedList);

        // filter by CatalogId
        expectedList = List.of(TestHelper.stubBundleDto(bundle3.getId(), List.of(bundleGroupVersion3)));
        resultActions = executeOkGetBundlesRequest(null, catalog2.getId());
        AssertionHelper.assertOnBundles(resultActions, expectedList);
    }


    @Test
    @WithMockUser(roles = {MANAGER})
    void aNonAdminUserShouldBeAbleToAccessTheExpectedBundles() throws Exception {

        // given I am logged but not as an admin
        when(securityHelperService.isUserAuthenticated()).thenReturn(true);
        when(securityHelperService.isAdmin()).thenReturn(false);
        when(securityHelperService.getContextAuthenticationUsername()).thenReturn(TestHelper.NON_ADMIN_USERNAME);

        // without filters should get all (and only) public bundles
        List<Bundle> expectedList = List.of(
                TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)),
                TestHelper.stubBundleDto(bundle2.getId(), List.of(bundleGroupVersion2)),
                TestHelper.stubBundleDto(bundle3.getId(), List.of(bundleGroupVersion3)));
        ResultActions resultActions = executeOkGetBundlesRequest(null, null);
        AssertionHelper.assertOnBundles(resultActions, expectedList);

        // filter by bundleGroupVersionId and catalogId
        expectedList = List.of(TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)));
        resultActions = executeOkGetBundlesRequest(bundleGroupVersion1.getId(), catalog1.getId());
        AssertionHelper.assertOnBundles(resultActions, expectedList);

        // filter by bundleGroupVersionId will return only public bundles
        expectedList = List.of(TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)));
        resultActions = executeOkGetBundlesRequest(bundleGroupVersion1.getId(), null);
        AssertionHelper.assertOnBundles(resultActions, expectedList);

        // filter by bundleGroupVersionId will not return anything if no public bundles are available
        resultActions = executeOkGetBundlesRequest(bundleGroupVersion4.getId(), null);
        AssertionHelper.assertOnBundles(resultActions, Collections.emptyList());

        // filter by CatalogId
        expectedList = List.of(TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)),
                TestHelper.stubBundleDto(bundle2.getId(), List.of(bundleGroupVersion2)));
        resultActions = executeOkGetBundlesRequest(null, catalog1.getId());
        AssertionHelper.assertOnBundles(resultActions, expectedList);
    }

    @Test
    void shouldReturnErrorWhileANonAdminUserAsksForProtectedData() throws Exception {

        // given I am logged but not as an admin
        when(securityHelperService.isUserAuthenticated()).thenReturn(true);
        when(securityHelperService.isAdmin()).thenReturn(false);
        when(securityHelperService.getContextAuthenticationUsername()).thenReturn(TestHelper.NON_ADMIN_USERNAME);

        // filter by bundleGroupVersionId and CatalogId (catalog to which the user doesn't belong to)
        executeGetBundlesRequest(bundleGroupVersion2.getId(), catalog2.getId(), StatusResultMatchers::isNotFound);

        // filter by CatalogId (catalog to which the user doesn't belong to)
        executeGetBundlesRequest(null, catalog2.getId(), StatusResultMatchers::isNotFound);

        // filter by bundleGroupVersionId (bundleGroupVersion that belong to an org to which the user doesn't belong to)
        executeGetBundlesRequest(bundleGroupVersion2.getId(), null, StatusResultMatchers::isNotFound);
    }

    @Test
    void anNonLoggedUserShouldBeAbleToAccessTheExpectedBundle() throws Exception {

        // given I am an admin
        when(securityHelperService.isUserAuthenticated()).thenReturn(false);

        // without filters should get every public bundle
        List<Bundle> expectedList = List.of(
                TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)),
                TestHelper.stubBundleDto(bundle2.getId(), List.of(bundleGroupVersion2)),
                TestHelper.stubBundleDto(bundle3.getId(), List.of(bundleGroupVersion3)));
        ResultActions resultActions = executeOkGetBundlesRequest(null, null);
        AssertionHelper.assertOnBundles(resultActions, expectedList);

        // filter by bundleGroupVersionId and public bundle group
        expectedList = List.of(TestHelper.stubBundleDto(bundle1.getId(), List.of(bundleGroupVersion1)));
        resultActions = executeOkGetBundlesRequest(bundleGroupVersion1.getId(), null);
        AssertionHelper.assertOnBundles(resultActions, expectedList);
    }

    @Test
    void shouldNotReturnDataWhileNonLoggedAsksForProtectedData() throws Exception {

        // given I am an admin
        when(securityHelperService.isUserAuthenticated()).thenReturn(false);

        // filter by bundleGroupVersionId and CatalogId
        executeGetBundlesRequest(bundleGroupVersion1.getId(), catalog1.getId(), StatusResultMatchers::isForbidden);

        // filter by bundleGroupVersionId and only private bundle group
        ResultActions resultActions = executeOkGetBundlesRequest(bundleGroupVersion4.getId(), null);
        AssertionHelper.assertOnBundles(resultActions, Collections.emptyList());

        // filter by CatalogId
        executeGetBundlesRequest(null, catalog2.getId(), StatusResultMatchers::isForbidden);
    }

    @Test
    @WithMockUser(roles = {ADMIN})
    void shouldNotReturnDataWithInconsistentParameters() throws Exception {

        // given I am an admin but this behaviour should be the same in every case
        when(securityHelperService.isUserAuthenticated()).thenReturn(true);

        // with non existing catalogId
        executeGetBundlesRequest(null, 999L, StatusResultMatchers::isNotFound);

        // with non existing bundleGroupVersionId
        executeGetBundlesRequest(999L, null, StatusResultMatchers::isNotFound);

        // with inconsistent bundleGroupVersionId and catalogId
        executeGetBundlesRequest(bundleGroupVersion1.getId(), catalog2.getId(), StatusResultMatchers::isNotFound);
    }


    private ResultActions executeOkGetBundlesRequest(Long bundleGroupVersionId, Long catalogId) throws Exception {
        return executeGetBundlesRequest(bundleGroupVersionId, catalogId, StatusResultMatchers::isOk);
    }

    private ResultActions executeGetBundlesRequest(Long bundleGroupVersionId, Long catalogId,
            StatusMatcher statusMatcher) throws Exception {
        String url = "/?";
        url += bundleGroupVersionId != null ? "bundleGroupVersionId=" + bundleGroupVersionId + "&" : "";
        url += catalogId != null ? "catalogId=" + catalogId + "&" : "";
        url = url.substring(0, url.length() - 1);

        return executeRequest(url, statusMatcher);
    }

//    private ResultActions executeFilteredRequest(String url) throws Exception {
//        return executeRequest("/filtered?page=0&pageSize=10" + url);
//    }

    private ResultActions executeRequest(String url, StatusMatcher statusMatcher) throws Exception {

        return mockMvc.perform(MockMvcRequestBuilders.get(
                                BASE_URL + url)
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(statusMatcher.checkStatus(status()));
//                .andExpect(status().isOk());
    }
}
