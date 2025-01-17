package com.entando.hub.catalog.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.entando.hub.catalog.persistence.entity.Catalog;
import com.entando.hub.catalog.persistence.entity.Organisation;
import com.entando.hub.catalog.service.CatalogService;
import com.entando.hub.catalog.service.dto.CatalogDTO;
import com.entando.hub.catalog.service.exception.ConflictException;
import com.entando.hub.catalog.service.exception.NotFoundException;
import com.entando.hub.catalog.service.security.SecurityHelperService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.entando.hub.catalog.testhelper.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    @Mock
    private CatalogService catalogService;
    @Mock
    private SecurityHelperService securityHelperService;

    private CatalogController catalogController;

    @BeforeEach
    void setUp() {
        this.catalogController = new CatalogController(catalogService, securityHelperService);
    }

    @Test
    void shouldGetCatalogs() {
        List<Catalog> expectedCatalogs = Arrays.asList(stubCatalog(), stubCatalog());
        List<CatalogDTO> expectedCatalogsDTO = Arrays.asList(stubCatalogDTO(), stubCatalogDTO());
        when(catalogService.getCatalogs(anyString(), anyBoolean())).thenReturn(expectedCatalogs);
        when(securityHelperService.getContextAuthenticationUsername()).thenReturn("admin");
        when(securityHelperService.isAdmin()).thenReturn(true);

        ResponseEntity<List<CatalogDTO>> responseEntity = catalogController.getCatalogs();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CatalogDTO> actualCatalogsDTO = responseEntity.getBody();
        assertThat(actualCatalogsDTO).usingRecursiveComparison().isEqualTo(expectedCatalogsDTO);
    }

    @Test
    void shouldGetEmptyListOfCatalogs() {

        when(catalogService.getCatalogs(anyString(), anyBoolean())).thenReturn(Collections.emptyList());
        when(securityHelperService.getContextAuthenticationUsername()).thenReturn("admin");
        when(securityHelperService.isAdmin()).thenReturn(true);

        ResponseEntity<List<CatalogDTO>> responseEntity = catalogController.getCatalogs();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEmpty();
    }

    @Test
    void shouldGetCatalogById() throws NotFoundException {
        Catalog expectedCatalog = stubCatalog();
        CatalogDTO expectedCatalogDTO = stubCatalogDTO();
        Long catalogId = expectedCatalogDTO.getId();

        when(this.securityHelperService.isAdmin()).thenReturn(true);
        when(this.securityHelperService.getContextAuthenticationUsername()).thenReturn(TestHelper.ADMIN_USERNAME);
        when(catalogService.getCatalogById(TestHelper.ADMIN_USERNAME, catalogId, true)).thenReturn(expectedCatalog);

        ResponseEntity<CatalogDTO> responseEntity = catalogController.getCatalog(catalogId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        CatalogDTO actualCatalogDTO = responseEntity.getBody();
        assertThat(actualCatalogDTO).usingRecursiveComparison().isEqualTo(expectedCatalogDTO);
    }


    @Test
    void shouldCreateCatalog() throws Exception {
        CatalogDTO expectedCatalogDTO = stubCatalogDTO();
        Long organisationId = expectedCatalogDTO.getOrganisationId();

        when(catalogService.createCatalog(organisationId)).thenReturn(stubCatalog());

        ResponseEntity<CatalogDTO> responseEntity = catalogController.createCatalog(organisationId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        CatalogDTO actualCatalogDTO = responseEntity.getBody();
        assertThat(actualCatalogDTO).usingRecursiveComparison().isEqualTo(expectedCatalogDTO);
    }

    @Test
    void shouldReturnConflictWithCreateCatalogWhenAlreadyExists() throws Exception {
        Long organisationId = 1L;

        when(catalogService.createCatalog(organisationId)).thenThrow(ConflictException.class);

        ResponseEntity<CatalogDTO> responseEntity = catalogController.createCatalog(organisationId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }


    @Test
    void shouldReturnNotFoundWithCreateCatalogWhenOrganisationNotExists() throws Exception {
        Long organisationId = 1L;

        when(catalogService.createCatalog(organisationId)).thenThrow(NotFoundException.class);

        ResponseEntity<CatalogDTO> responseEntity = catalogController.createCatalog(organisationId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }



    @Test
    void shouldDeleteCatalog() throws Exception {
        Catalog catalog = stubCatalog();
        CatalogDTO expectedCatalogDTO = stubCatalogDTO();
        Long catalogId = expectedCatalogDTO.getId();

        when(catalogService.deleteCatalog(catalogId)).thenReturn(catalog);

        ResponseEntity<CatalogDTO> responseEntity = catalogController.deleteCatalog(catalogId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        CatalogDTO actualCatalogDTO = responseEntity.getBody();
        assertThat(actualCatalogDTO).usingRecursiveComparison().isEqualTo(expectedCatalogDTO);

    }

    @Test
    void shouldReturnNotFoundWhenDeleteCatalog() throws Exception {
        Long catalogId = 1L;

        when(catalogService.deleteCatalog(catalogId)).thenThrow(NotFoundException.class);

        ResponseEntity<CatalogDTO> responseEntity = catalogController.deleteCatalog(catalogId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }



    private CatalogDTO stubCatalogDTO(){
        return new CatalogDTO(1L, 2L, "Entando private catalog");
    }
    private Catalog stubCatalog(){
        return new Catalog().setId(1L).setName("Entando private catalog").setOrganisation(new Organisation().setId(2L));
    }
}
