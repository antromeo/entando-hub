package com.entando.hub.catalog.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.entando.hub.catalog.persistence.BundleGroupRepository;
import com.entando.hub.catalog.persistence.BundleGroupVersionRepository;
import com.entando.hub.catalog.persistence.BundleRepository;
import com.entando.hub.catalog.persistence.CategoryRepository;
import com.entando.hub.catalog.persistence.entity.Bundle;
import com.entando.hub.catalog.persistence.entity.BundleGroup;
import com.entando.hub.catalog.persistence.entity.BundleGroupVersion;
import com.entando.hub.catalog.persistence.entity.Category;
import com.entando.hub.catalog.persistence.entity.Organisation;
import com.entando.hub.catalog.response.BundleGroupVersionFilteredResponseView;
import com.entando.hub.catalog.rest.BundleGroupVersionController.BundleGroupVersionView;
import com.entando.hub.catalog.rest.PagedContent;

@Service
public class BundleGroupVersionService {

    private final Logger logger = LoggerFactory.getLogger(BundleGroupVersionService.class);
    private final BundleGroupVersionRepository bundleGroupVersionRepository;
    final private BundleGroupRepository bundleGroupRepository;
    final private BundleRepository bundleRepository;
    final private CategoryRepository categoryRepository;

    @Autowired
    private Environment environment;

    public BundleGroupVersionService(BundleGroupVersionRepository bundleGroupVersionRepository, BundleGroupRepository bundleGroupRepository,BundleRepository bundleRepository,CategoryRepository categoryRepository) {
    	this.bundleGroupVersionRepository = bundleGroupVersionRepository;
    	this.bundleGroupRepository = bundleGroupRepository;
    	this.bundleRepository = bundleRepository;
    	this.categoryRepository = categoryRepository;
    }
    public Optional<BundleGroupVersion> getBundleGroupVersion(String bundleGroupVersionId) {
        return bundleGroupVersionRepository.findById(Long.parseLong(bundleGroupVersionId));
    }

    
    @Transactional
    public BundleGroupVersion createBundleGroupVersion(BundleGroupVersion bundleGroupVersionEntity, BundleGroupVersionView bundleGroupVersionView) {
    	if (bundleGroupVersionView.getStatus().equals(BundleGroupVersion.Status.PUBLISHED)) {
    		List<BundleGroupVersion> publishedBundles = bundleGroupVersionRepository.findByBundleGroupAndStatus(bundleGroupVersionEntity.getBundleGroup(), BundleGroupVersion.Status.PUBLISHED);
    		if (!publishedBundles.isEmpty()) {
    			for(BundleGroupVersion publishedBundle : publishedBundles) {
    				publishedBundle.setStatus(BundleGroupVersion.Status.ARCHIVE);
    			}
    			bundleGroupVersionRepository.saveAll(publishedBundles);
    		}
    	}
    	bundleGroupVersionEntity.setLastUpdated(LocalDateTime.now());
//    	kamlesh commented, need to remove
//    	if (bundleGroupVersionView.getChildren() != null) {
//            //TODO native query to improve performance
//            bundleRepository.findByBundleGroupsIs(bundleGroupVersionEntity.getBundleGroup()).stream().forEach(bundle -> {
//                bundle.getBundleGroups().remove(bundleGroupVersionEntity.getBundleGroup());
//                bundleRepository.save(bundle);
//            });
//            Set<Bundle> bundleSet = bundleGroupVersionView.getChildren().stream().map((bundleChildId) -> {
//                com.entando.hub.catalog.persistence.entity.Bundle bundle = bundleRepository.findById(Long.valueOf(bundleChildId)).get();
//                bundle.getBundleGroups().add(bundleGroupVersionEntity.getBundleGroup());
//                bundleRepository.save(bundle);
//                return bundle;
//            }).collect(Collectors.toSet());
//            bundleGroupVersionEntity.getBundleGroup().setBundles(bundleSet);
//        }
    	
//      if (bundleGroup.getChildren() != null) {
//      //TODO native query to improve performance
//      bundleRepository.findByBundleGroupsIs(toUpdate).stream().forEach(bundle -> {
//          bundle.getBundleGroups().remove(toUpdate);
//          bundleRepository.save(bundle);
//      });
//      Set<Bundle> bundleSet = bundleGroup.getChildren().stream().map((bundleChildId) -> {
//          com.entando.hub.catalog.persistence.entity.Bundle bundle = bundleRepository.findById(Long.valueOf(bundleChildId)).get();
//          bundle.getBundleGroups().add(toUpdate);
//          bundleRepository.save(bundle);
//          return bundle;
//      }).collect(Collectors.toSet());
//      toUpdate.setBundles(bundleSet);
//  }
    	BundleGroupVersion entity = bundleGroupVersionRepository.save(bundleGroupVersionEntity);
    	
    	if (bundleGroupVersionView.getChildren() != null) {
  	      //TODO native query to improve performance
  	      bundleRepository.findByBundleGroupVersionsIs(entity).stream().forEach(bundle -> {
  	          bundle.getBundleGroupVersions().remove(entity);
  	          bundleRepository.save(bundle);
  	      });
  	      Set<Bundle> bundleSet = bundleGroupVersionView.getChildren().stream().map((bundleChildId) -> {
  	          com.entando.hub.catalog.persistence.entity.Bundle bundle = bundleRepository.findById(Long.valueOf(bundleChildId)).get();
  	          bundle.getBundleGroupVersions().add(entity);
  	          bundleRepository.save(bundle);
  	          return bundle;
  	      }).collect(Collectors.toSet());
  	    entity.setBundles(bundleSet);
  	}
      
//    	BundleGroupVersion entity = bundleGroupVersionRepository.save(bundleGroupVersionEntity);
    	

    	return entity;
    }
    
    public PagedContent<BundleGroupVersionFilteredResponseView, BundleGroupVersion> getBundleGroupVersions(Integer pageNum, Integer pageSize, Optional<String> organisationId, String[] categoryIds, String[] statuses) {
        Pageable paging;
        if (pageSize == 0) {
            paging = Pageable.unpaged();
        } else {
            Sort.Order order = new Sort.Order(Sort.Direction.DESC, "lastUpdated");
            paging = PageRequest.of(pageNum, pageSize, Sort.by(order));
        }
        Set<Category> categories = Arrays.stream(categoryIds).map(cid -> {
            Category category = new Category();
            category.setId(Long.valueOf(cid));
            return category;
        }).collect(Collectors.toSet());

       Set<BundleGroupVersion.Status> statusSet = Arrays.stream(statuses).map(BundleGroupVersion.Status::valueOf).collect(Collectors.toSet());
        List<BundleGroup> bunleGroups ;
        if (organisationId.isPresent()) {
            Organisation organisation = new Organisation();
            organisation.setId(Long.valueOf(organisationId.get()));
            bunleGroups = bundleGroupRepository.findDistinctByOrganisationAndCategoriesIn(
                    organisation,
                    categories);
        }else {
        	bunleGroups = bundleGroupRepository.findDistinctByCategoriesIn(categories);
        }
        
        Page<BundleGroupVersion> page = bundleGroupVersionRepository.findByBundleGroupInAndStatusIn(bunleGroups, statusSet, paging);  
        PagedContent<BundleGroupVersionFilteredResponseView, BundleGroupVersion> pagedContent = new PagedContent<>(toResponseViewList(page).stream()
        	.sorted(Comparator.comparing(BundleGroupVersionFilteredResponseView::getName, String::compareToIgnoreCase))
        	.collect(Collectors.toList()), page);
        return pagedContent;
    }
    
    /**
     * Set bundle group url
     * @param bundleGroupVersionId
     * @return
     */
	private String getBundleGroupUrl(Long bundleGroupVersionId) {
		String hubGroupDeatilUrl = environment.getProperty("HUB_GROUP_DETAIL_BASE_URL");
		if (Objects.nonNull(hubGroupDeatilUrl)) {
			return hubGroupDeatilUrl + "bundlegroupversion/" + bundleGroupVersionId;
		}
		return "";
	}
	
		 public PagedContent<BundleGroupVersionFilteredResponseView, com.entando.hub.catalog.persistence.entity.BundleGroupVersion>  getBundleGroupVersions(Integer pageNum, Integer pageSize, String[] statuses, BundleGroup bundleGroup) {
	        Pageable paging;
	        if (pageSize == 0) {
	            paging = Pageable.unpaged();
	        } else {
	        	   Sort.Order order = new Sort.Order(Sort.Direction.DESC, "lastUpdated");
	            paging = PageRequest.of(pageNum, pageSize, Sort.by(order));
	        }
	
	        Set<BundleGroupVersion.Status> statusSet = Arrays.stream(statuses).map(BundleGroupVersion.Status::valueOf).collect(Collectors.toSet());
	        Page<BundleGroupVersion> page = bundleGroupVersionRepository.findByBundleGroupAndStatusIn(bundleGroup, statusSet, paging);

	        List<BundleGroupVersion> versions = new ArrayList<BundleGroupVersion>();
	        versions.addAll(page.getContent().stream().filter(version -> !version.getStatus().equals(BundleGroupVersion.Status.ARCHIVE)).collect(Collectors.toList()));
	        versions.addAll(page.getContent().stream().filter(version -> version.getStatus().equals(BundleGroupVersion.Status.ARCHIVE)).collect(Collectors.toList()));
	        Page<BundleGroupVersion> pageResponse = new PageImpl<>(versions);

	        PagedContent<BundleGroupVersionFilteredResponseView, BundleGroupVersion> pagedContent = new PagedContent<>(toResponseViewList(pageResponse).stream()
	        		  .sorted(Comparator.comparing(BundleGroupVersionFilteredResponseView::getName, String::compareToIgnoreCase))
	        		  .collect(Collectors.toList()), pageResponse);
	        return pagedContent;
	 }
	

		@Transactional
		public void deleteBundleGroupVersion(Optional<BundleGroupVersion> bundleGroupVersionOptional) {
			bundleGroupVersionOptional.ifPresent(bundleGroupVersion -> {
//				bundleGroupVersionRepository.delete(bundleGroupVersion);//kamlesh commented
//				List<BundleGroupVersion> versions = bundleGroupVersionRepository.findByBundleGroup(bundleGroupVersion.getBundleGroup());
//				if (versions.isEmpty()) {
//					deleteFromCategories(bundleGroupVersion.getBundleGroup());
					deleteFromBundles(bundleGroupVersion);
//					bundleGroupRepository.delete(bundleGroupVersion.getBundleGroup());//kamlesh commented not required
					bundleGroupVersionRepository.delete(bundleGroupVersion);
//				}
			});
		}

		private void deleteFromCategories(BundleGroup bundleGroup) {
			bundleGroup.getCategories().forEach((category) -> {
				category.getBundleGroups().remove(bundleGroup);
				categoryRepository.save(category);
			});
		}

//		Kamlesh commented, need to remove later
//		private void deleteFromBundles(BundleGroup bundleGroup) {
//			bundleGroup.getBundles().forEach((bundle) -> {
//				bundle.getBundleGroups().remove(bundleGroup);
//				bundleRepository.save(bundle);
//				
//			});
//		}
//		kamlesh: replaced above code.
		private void deleteFromBundles(BundleGroupVersion bundleGroupVersion) {
			bundleGroupVersion.getBundles().forEach((bundle) -> {
				bundle.getBundleGroupVersions().remove(bundleGroupVersion);
				bundleRepository.save(bundle);
			});
		}
	    
	  public List<BundleGroupVersion> getBundleGroupVersions(com.entando.hub.catalog.persistence.entity.BundleGroup bundleGroup, String version ){
	        return bundleGroupVersionRepository.findByBundleGroupAndVersion(bundleGroup, version);
	  }

	/**
	 * If a bundle group has 1 or no bundle group versions then it is editable.
	 * 
	 * @param bundleGroup
	 * @return
	 */
	public boolean isBundleGroupEditable(BundleGroup bundleGroup) {
		if (bundleGroupVersionRepository.countByBundleGroup(bundleGroup) <= 1) {
			return true;
		}
		return false;
	}

	/**
	 * Check if the current bundle is editable or not
	 * 
	 * @param bundleGroup
	 * @return
	 */
	public boolean canAddNewVersion(BundleGroupVersion bundleGroupVersion) {
		Set<BundleGroupVersion.Status> statuses = new HashSet<BundleGroupVersion.Status>();
		statuses.add(BundleGroupVersion.Status.NOT_PUBLISHED);
		statuses.add(BundleGroupVersion.Status.PUBLISHED);
		List<BundleGroupVersion> versions = bundleGroupVersionRepository.findByBundleGroupVersionAndStatusIn(bundleGroupVersion, statuses);
		if (!CollectionUtils.isEmpty(versions) && versions.size() > 1) {
			return false;
		}
		return true;
	}

	/**
	 * Convert to response view list
	 * 
	 * @param page
	 * @return
	 */
	private List<BundleGroupVersionFilteredResponseView> toResponseViewList(Page<BundleGroupVersion> page) {
		List<BundleGroupVersionFilteredResponseView> list = new ArrayList<BundleGroupVersionFilteredResponseView>();
		page.getContent().stream().forEach((entity) -> {
			BundleGroupVersionFilteredResponseView viewObj = new BundleGroupVersionFilteredResponseView();
			viewObj.setBundleGroupVersionId(entity.getId());
			viewObj.setDescription(entity.getDescription());
			viewObj.setDescriptionImage(entity.getDescriptionImage());
			viewObj.setStatus(entity.getStatus());
			viewObj.setDocumentationUrl(entity.getDocumentationUrl());
			viewObj.setVersion(entity.getVersion());
			viewObj.setBundleGroupUrl(getBundleGroupUrl(entity.getId()));

			viewObj.setCreatedAt(entity.getCreatedAt());
			viewObj.setLastUpdate(entity.getLastUpdated());
			
			if (!CollectionUtils.isEmpty(entity.getBundles())) {
				viewObj.setChildren(entity.getBundles().stream()
						.map(child -> child.getId().toString()).collect(Collectors.toList()));
			}

			if (Objects.nonNull(entity.getBundleGroup())) {
				viewObj.setName(entity.getBundleGroup().getName());
				viewObj.setBundleGroupId(entity.getBundleGroup().getId());
				viewObj.setIsEditable(isBundleGroupEditable(entity.getBundleGroup()));
				viewObj.setCanAddNewVersion(canAddNewVersion(entity));
				if (Objects.nonNull(entity.getBundleGroup().getOrganisation())) {
					viewObj.setOrganisationId(entity.getBundleGroup().getOrganisation().getId());
					viewObj.setOrganisationName(entity.getBundleGroup().getOrganisation().getName());
				}
				if (!CollectionUtils.isEmpty(entity.getBundleGroup().getCategories())) {
					viewObj.setCategories(entity.getBundleGroup().getCategories().stream()
							.map((category) -> category.getId().toString()).collect(Collectors.toList()));
				}
				if (!CollectionUtils.isEmpty(entity.getBundleGroup().getVersion())) {
					viewObj.setAllVersions(entity.getBundleGroup().getVersion().stream()
							.map(version -> version.getVersion().toString()).collect(Collectors.toList()));
				}
			}
			list.add(viewObj);
		});
		return list;
	}
}
