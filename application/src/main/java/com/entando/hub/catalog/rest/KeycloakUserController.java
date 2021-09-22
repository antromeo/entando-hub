package com.entando.hub.catalog.rest;

import com.entando.hub.catalog.rest.model.SearchKeycloackUserRequest;
import com.entando.hub.catalog.service.KeycloakService;
import com.entando.hub.catalog.service.model.UserRepresentation;
import java.util.Date;
import java.util.HashMap;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/keycloack")
@Slf4j
public class KeycloakUserController {

    final private KeycloakService keycloakService;

    public KeycloakUserController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }
    
    @CrossOrigin
    @GetMapping("/users")
    public List<RestUserRepresentation> searchUsers(SearchKeycloackUserRequest request) {
        Map<String, String> map = (null != request) ? request.getParams() : new HashMap<>();
        return this.keycloakService.searchUsers(map).stream().map(RestUserRepresentation::new).collect(Collectors.toList());
    }
    
    @CrossOrigin
    @GetMapping("/users/{username}")
    public ResponseEntity<RestUserRepresentation> getUser(@PathVariable String username) {
        UserRepresentation user = this.keycloakService.getUser(username);
        if (null == user) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new RestUserRepresentation(user), HttpStatus.OK);
    }
    
    
    @Getter
    @Setter
    @ToString
    public static class RestUserRepresentation {

        private String id;
        private Date created;
        private String username;
        private boolean enabled;
        private String firstName;
        private String lastName;
        private String email;

        public RestUserRepresentation(com.entando.hub.catalog.service.model.UserRepresentation user) {
            this.id = user.getId();
            this.created = new Date(user.getCreatedTimestamp());
            this.username = user.getUsername();
            this.enabled = user.isEnabled();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
        }
    }

}