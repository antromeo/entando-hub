package com.entando.hub.catalog.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;

import com.entando.hub.catalog.persistence.BundleGroupRepository;
import com.entando.hub.catalog.persistence.OrganisationRepository;
import com.entando.hub.catalog.persistence.entity.Organisation;
import com.entando.hub.catalog.rest.OrganisationController;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(MockitoJUnitRunner.Silent.class)
public class OrganisationServiceTest {
	@InjectMocks
	OrganisationService organisationService;
	@Mock
	OrganisationRepository organisationRepository;
	@Mock
	BundleGroupRepository bundleGroupRepository;
	
	@Test
	public void updateMappedByTest() {
		Organisation organisation = new Organisation();
		organisation.setId(3001L);
		organisation.setName("Technical");
		organisation.setDescription("New Organisation");
		com.entando.hub.catalog.persistence.entity.BundleGroup bundleGroup = new com.entando.hub.catalog.persistence.entity.BundleGroup();
		bundleGroup.setId(1001L);
		bundleGroup.setName("New Xyz");
		Set<com.entando.hub.catalog.persistence.entity.BundleGroup> bundleGroups = new HashSet<>();
		bundleGroups.add(bundleGroup);
		organisation.setBundleGroups(bundleGroups);
		OrganisationController.OrganisationNoId OrganisationNoId = new OrganisationController.OrganisationNoId(organisation) ;
		bundleGroupRepository.findByOrganisationId(organisation.getId());
		bundleGroup.setOrganisation(null); //this is the mappedBy field
	    bundleGroupRepository.save(bundleGroup);
	    Set<com.entando.hub.catalog.persistence.entity.BundleGroup> newBundleGroups = new HashSet<>();
		Mockito.when(bundleGroupRepository.findById(Long.valueOf(bundleGroup.getId()))).thenReturn(Optional.of(bundleGroup));
		bundleGroup.setOrganisation(organisation);
		bundleGroupRepository.save(bundleGroup);
		organisation.setBundleGroups(newBundleGroups);
	    organisationService.updateMappedBy(organisation, OrganisationNoId);
    
		
	}
	
	@Test
	public void getOrganisationsTest() {
		List<Organisation> organisationList = new ArrayList<>();
		Organisation organisation = new Organisation();
		organisation.setId(3001L);
		organisation.setName("Technical");
		organisation.setDescription("New Organisation");
		organisationList.add(organisation);
		Mockito.when(organisationRepository.findAll(Sort.by(Sort.Order.asc("name")))).thenReturn(organisationList);
		List<Organisation> organisationResult = organisationService.getOrganisations();
		assertNotNull(organisationResult);
		assertEquals(organisationList.get(0).getId(), organisationResult.get(0).getId());
		assertEquals(organisationList.get(0).getName(), organisationResult.get(0).getName());
		assertEquals(organisationList.get(0).getDescription(), organisationResult.get(0).getDescription());
}
	@Test
	public void getOrganisationTest() {
		Organisation organisation = new Organisation();
		Optional<Organisation> organisationList = Optional.of(organisation);
		organisation.setId(3001L);
		organisation.setName("Technical");
		organisation.setDescription("New Organisation");
		String organisationId = Long.toString(organisation.getId());
		Mockito.when(organisationRepository.findById(Long.parseLong(organisationId))).thenReturn(organisationList);
		Optional<Organisation> organisationResult = organisationService.getOrganisation(organisationId);
		assertNotNull(organisationResult);
		assertEquals(organisationList.get().getId(), organisationResult.get().getId());
		assertEquals(organisationList.get().getName(), organisationResult.get().getName());
		assertEquals(organisationList.get().getDescription(), organisationResult.get().getDescription());
	}
	
	@Test
	public void createOrganisationTest() {
		//List<Organisation> organisationList = new ArrayList<>();
		Organisation organisation = new Organisation();
		organisation.setId(3001L);
		organisation.setName("Technical");
		organisation.setDescription("New Organisation");
		OrganisationController OrganisationController = new OrganisationController(organisationService);
		OrganisationController.OrganisationNoId OrganisationNoId = new OrganisationController.OrganisationNoId(organisation) ;
		Mockito.when(organisationRepository.save(organisation)).thenReturn(organisation);
		organisationService.updateMappedBy(organisation, OrganisationNoId);
		Organisation organisationResult = organisationService.createOrganisation(organisation ,OrganisationNoId);
		assertNotNull(organisationResult);
		assertEquals(organisation, organisationResult);
}
	
	@Test
	public void deleteOrganisationTest() {
		Organisation organisation = new Organisation();
		organisation.setId(3001L);
		organisation.setName("Technical");
		organisation.setDescription("New Organisation");
		organisationRepository.deleteById(organisation.getId());
		String organisationId = String.valueOf(organisation.getId());
		organisationService.deleteOrganisation(organisationId);
}

}
