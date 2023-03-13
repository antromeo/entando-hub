package com.entando.hub.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.entando.hub.catalog.persistence.CatalogRepository;
import com.entando.hub.catalog.persistence.entity.Catalog;
import com.entando.hub.catalog.persistence.entity.Organisation;
import com.entando.hub.catalog.service.exception.ConflictException;
import com.entando.hub.catalog.service.exception.NotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.entando.hub.catalog.testhelper.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CatalogRepository catalogRepository;
    @Mock
    private OrganisationService organisationService;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
            this.catalogService = new CatalogService(catalogRepository, organisationService);
    }

    @Test
    void shouldReturnAllCatalogsWhileAdmin() {
        List<Catalog> expectedCatalogs = Arrays.asList(stubCatalog());
        when(this.catalogRepository.findAll()).thenReturn(expectedCatalogs);

        List<Catalog> actualCatalogs = catalogService.getCatalogs("my-username", true);

        assertThat(actualCatalogs).hasSize(expectedCatalogs.size());
        assertThat(actualCatalogs).usingRecursiveComparison().isEqualTo(expectedCatalogs);
        verify(this.catalogRepository, never()).findByOrganisation_PortalUsers_Username(anyString());
    }

    @Test
    void shouldReturnCatalogsBelongingToTheReceivedUserWhileNONAdmin() {
        List<Catalog> expectedCatalogs = Arrays.asList(stubCatalog());
        when(this.catalogRepository.findByOrganisation_PortalUsers_Username(anyString())).thenReturn(expectedCatalogs);

        List<Catalog> actualCatalogs = catalogService.getCatalogs("my-username", false);

        assertThat(actualCatalogs).hasSize(expectedCatalogs.size());
        assertThat(actualCatalogs).usingRecursiveComparison().isEqualTo(expectedCatalogs);
        verify(this.catalogRepository, never()).findAll();
    }

    @Test
    void shouldReturnEmptyCatalogList() {
        when(this.catalogRepository.findAll()).thenReturn(new ArrayList<>());

        List<Catalog> actualCatalogsDTO = catalogService.getCatalogs("my-username", true);

        assertThat(actualCatalogsDTO).hasSize(0);
    }

    @Test
    void shouldReturnCatalogById() throws NotFoundException {
        Long id = 1L;
        Catalog expectedCatalog = stubCatalog();
        when(this.catalogRepository.findById(id)).thenReturn(Optional.ofNullable(expectedCatalog));

        Catalog actualCatalog = catalogService.getCatalogById(TestHelper.ADMIN_USERNAME, id, true);

        assertThat(actualCatalog).usingRecursiveComparison().isEqualTo(expectedCatalog);
    }

    @Test
    void shouldReturnNotFoundWhenCatalogNotExists() throws NotFoundException {
        Long id = 1L;
        when(this.catalogRepository.findById(id)).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> {
            catalogService.getCatalogById(TestHelper.ADMIN_USERNAME, id, true);
        });

        String expectedMessage = "Catalog not found";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    void shouldCreateCatalog() throws Exception {
        Organisation organisation = stubOrganisation();
        Long organisationId = organisation.getId();

        when(this.organisationService.getOrganisation(organisationId)).thenReturn(Optional.of(organisation));
        when(this.catalogRepository.existsByOrganisationId(organisationId)).thenReturn(false);
        String NameCatalog = organisation.getName() + " private catalog";
        Catalog savedCatalog = new Catalog().setId(1L).setOrganisation(organisation).setName(NameCatalog);
        when(catalogRepository.save(any(Catalog.class))).thenReturn(savedCatalog);

        Catalog actualCatalog = catalogService.createCatalog(organisationId);

        assertThat(actualCatalog)
                .hasFieldOrPropertyWithValue("id", savedCatalog.getId())
                .hasFieldOrPropertyWithValue("organisation.id", organisation.getId())
                .hasFieldOrPropertyWithValue("name", NameCatalog);

    }

    @Test
    void shouldReturnNotFoundWhenOrganisationIsNotFound() {
        Long organisationId = 1L;
        when(organisationService.getOrganisation(organisationId)).thenReturn(Optional.empty());
        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> {
            catalogService.createCatalog(organisationId);
        });

        String expectedMessage = "Organisation not found";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldReturnConflictWhenCatalogAlreadyExistsForOrganisation() {
        Organisation organisation = stubOrganisation();
        Long organisationId = organisation.getId();
        when(organisationService.getOrganisation(organisationId)).thenReturn(Optional.of(organisation));
        when(catalogRepository.existsByOrganisationId(organisationId)).thenReturn(true);

        Exception exception = Assertions.assertThrows(ConflictException.class, () -> {
            catalogService.createCatalog(organisationId);
        });

        String expectedMessage = "Catalog already exists";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldDeleteCatalog() throws Exception {
        Catalog expectedCatalog = stubCatalog();
        Long catalogId = expectedCatalog.getId();

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(expectedCatalog));
        doNothing().when(catalogRepository).deleteById(catalogId);

        Catalog actualCatalog = catalogService.deleteCatalog(catalogId);

        assertThat(actualCatalog).usingRecursiveComparison().isEqualTo(expectedCatalog);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingCatalog() {
        Long catalogId = 1L;

        when(catalogRepository.findById(catalogId)).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> {
            catalogService.deleteCatalog(catalogId);
        });

        String expectedMessage = "Catalog not found";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }


    private Catalog stubCatalog(){
        return new Catalog().setId(1L).setName("Entando private catalog").setOrganisation(new Organisation().setId(2L));
    }
    private Organisation stubOrganisation(){
        return new Organisation().setId(2L).setName("Entando");
    }
}

