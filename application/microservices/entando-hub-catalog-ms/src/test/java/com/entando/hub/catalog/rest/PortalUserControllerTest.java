package com.entando.hub.catalog.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.entando.hub.catalog.rest.model.PortalUserResponseView;
import com.entando.hub.catalog.rest.model.UserOrganisationRequest;
import com.entando.hub.catalog.service.PortalUserService;
import com.entando.hub.catalog.service.security.SecurityHelperService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.entando.hub.catalog.service.model.UserRepresentation;
import com.entando.hub.catalog.rest.KeycloakUserController.RestUserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class PortalUserControllerTest {
    @Mock
    private PortalUserService portalUserService;
    @Mock
    private SecurityHelperService securityHelperService;

    private PortalUserController portalUserController;

    private final String ID = "1";
    private final String USERNAME = "Admin";
    private final String EMAIL = "admin.123@test.co.in";

    @BeforeEach
    void setUp() {
        this.portalUserController = new PortalUserController(portalUserService, securityHelperService);
    }


    @Test
    void shouldGetCatalogs(){
        List<UserRepresentation> expectedUsers = List.of(this.getMockUserRepresentation(), this.getMockUserRepresentation());
        List<RestUserRepresentation> expectedUsersDTO = this.getMockRestUserRepresentationByUsers(expectedUsers);

        when(portalUserService.getUsersByOrganisation(null)).thenReturn(expectedUsers);

        ResponseEntity<List<RestUserRepresentation>> responseEntity = portalUserController.getUsers(null);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<RestUserRepresentation> actualUsersDTO = responseEntity.getBody();
        assertThat(actualUsersDTO).usingRecursiveComparison().isEqualTo(expectedUsersDTO);
    }

    @Test
    void shouldGetPortalUserByUsernameFromToken(){
        PortalUserResponseView expectedPortalUser = this.getMockPortalUserResponseView();

        when(securityHelperService.getContextAuthenticationUsername()).thenReturn(USERNAME);
        when(portalUserService.getUserByUsername(USERNAME)).thenReturn(expectedPortalUser);

        ResponseEntity<PortalUserResponseView> responseEntity = portalUserController.getPortalUserByUsername();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(responseEntity.getBody()).usingRecursiveComparison().isEqualTo(expectedPortalUser);
    }

    @Test
    void shouldReturnNotFoundWhenCallGetPortalUserByUsernameFromTokenWithoutUsername(){
        when(securityHelperService.getContextAuthenticationUsername()).thenReturn(USERNAME);
        when(portalUserService.getUserByUsername(USERNAME)).thenReturn(null);

        ResponseEntity<PortalUserResponseView> responseEntity = portalUserController.getPortalUserByUsername();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldAddUserToOrganisation(){
        String organisationId = "1";
        UserOrganisationRequest request = this.getMockUserOrganisationRequest();

        when(portalUserService.addUserToOrganization(USERNAME, organisationId)).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> responseEntity = portalUserController.addUserToOrganisation(organisationId, request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).containsEntry("result", false);
    }

    @Test
    void shouldNotAddUserToOrganisation(){
        String organisationId = "1";
        UserOrganisationRequest request = this.getMockUserOrganisationRequest();

        when(portalUserService.addUserToOrganization(USERNAME, organisationId)).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> responseEntity = portalUserController.addUserToOrganisation(organisationId, request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).containsEntry("result", false);
    }

    @Test
    void shouldDeleteUserFromOrganisation(){
        String organisationId = "1";

        when(portalUserService.removeUserFromOrganization(USERNAME, organisationId)).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> responseEntity = portalUserController.deleteUserFromOrganisation(organisationId, USERNAME);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).containsEntry("result", true);
    }

    @Test
    void shouldNotDeleteUserFromOrganisation(){
        String organisationId = "1";

        when(portalUserService.removeUserFromOrganization(USERNAME, organisationId)).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> responseEntity = portalUserController.deleteUserFromOrganisation(organisationId, USERNAME);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).containsEntry("result", false);
    }

    @Test
    void shouldDeleteUser(){
        when(portalUserService.removeUser(USERNAME)).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> responseEntity = portalUserController.deleteUser(USERNAME);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).containsEntry("result", true);
    }

    @Test
    void shouldNotDeleteUser(){
        when(portalUserService.removeUser(USERNAME)).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> responseEntity = portalUserController.deleteUser(USERNAME);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).containsEntry("result", false);
    }


    private UserRepresentation getMockUserRepresentation(){
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(ID);
        userRepresentation.setUsername(USERNAME);
        userRepresentation.setEmail(EMAIL);
        return userRepresentation;
    }

    private List<RestUserRepresentation> getMockRestUserRepresentationByUsers(List<UserRepresentation> users){
        List <RestUserRepresentation> expectedRestUserRepresentations = new ArrayList<>();
        for ( UserRepresentation user : users ){
            expectedRestUserRepresentations.add(new RestUserRepresentation(user));
        }
        return expectedRestUserRepresentations;
    }

    private PortalUserResponseView getMockPortalUserResponseView(){
        PortalUserResponseView portalUserResponseView = new PortalUserResponseView();
        portalUserResponseView.setId(Long.parseLong(ID));
        portalUserResponseView.setUsername(USERNAME);
        portalUserResponseView.setEmail(EMAIL);
        return portalUserResponseView;
    }

    private UserOrganisationRequest getMockUserOrganisationRequest() {
        return new UserOrganisationRequest().setUsername(USERNAME);
    }

}
