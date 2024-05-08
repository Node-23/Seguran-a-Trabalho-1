package com.ufpi.segt1.Controllers;

import com.ufpi.segt1.DTO.KeyDTO;
import com.ufpi.segt1.Models.Key;
import com.ufpi.segt1.Services.KeyManagementService;
import com.ufpi.segt1.Services.KeyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/keys")
@CrossOrigin(origins = "*")
public class KeyController {
    private final KeyService keyService;
    private final KeyManagementService keyManagementService;

    public KeyController(KeyService service, KeyManagementService keyManagementService) {
        this.keyService = service;
        this.keyManagementService = keyManagementService;
    }

    @GetMapping
    public ResponseEntity<List<Key>> getAllKeys(){
        List<Key> keys = this.keyService.getAllKeys();
        return new ResponseEntity<>(keys, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Key> getKeyById(@PathVariable("id") Long id){
        Key key = this.keyService.findKeyById(id);
        return new ResponseEntity<>(key, HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<Key> createKey(@RequestBody KeyDTO key){
        Key newKey = keyService.createKey(key);
        keyManagementService.CreateKeyPair(newKey);
        return new ResponseEntity<>(newKey, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Key> updateKey(@PathVariable("id") Long id, @RequestBody KeyDTO keyDTO) {
        Key updatedKey = keyService.updateKey(id, keyDTO);
        if (updatedKey != null) {
            return new ResponseEntity<>(updatedKey, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyById(@PathVariable("id") Long id) {
        this.keyService.deleteKeyById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/encrypt")
    public ResponseEntity<String> encryptMessage(@RequestBody String message){
        String msg = keyManagementService.EncryptMessage(message);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }

    @PostMapping("/decrypt")
    public ResponseEntity<String> decryptMessage(@RequestBody String encryptedMessage){
        String msg = keyManagementService.DecryptMessage(encryptedMessage);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }

    //TODO: Import key from file
    //TODO: Export key to file
    //TODO: Forgot password
}
