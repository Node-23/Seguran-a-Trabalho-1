package com.ufpi.segt1.Services;

import com.ufpi.segt1.DTO.KeyDTO;
import com.ufpi.segt1.Exceptions.FieldAlreadyInUseException;
import com.ufpi.segt1.Exceptions.KeyNotFoundException;
import com.ufpi.segt1.Exceptions.PasswordRulesException;
import com.ufpi.segt1.Infra.S3Management;
import com.ufpi.segt1.Models.Key;
import com.ufpi.segt1.Repositories.KeyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KeyService {
    private final KeyRepository repository;
    private final UserService userService;
    private final S3Management s3Management;

    public KeyService(KeyRepository repository, UserService userService, S3Management s3Management) {
        this.repository = repository;
        this.userService = userService;
        this.s3Management = s3Management;
    }

    public List<Key> getAllKeys() {
        return this.repository.findAll();
    }

    public Key findKeyById(Long id){
        return repository.findKeyById(id).orElseThrow(KeyNotFoundException::new);
    }
    public Key createKey(KeyDTO keyDTO){
        Key newKey = new Key(keyDTO, userService.findUserById(keyDTO.ownerId()));
        this.SaveKey(newKey);
        return newKey;
    }

    private void SaveKey(Key key){
        ValidatePassword(key.getPassword());
        try {
            key.setPassword(GeneralService.encryptPasswords(key.getPassword()));
            this.repository.save(key);
        }catch (DataIntegrityViolationException ex){
            String constrainField = GeneralService.getConstrainField(ex);
            throw new FieldAlreadyInUseException(constrainField);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void ValidatePassword(String password){
        if (password.length() < 6){
            throw new PasswordRulesException("Senha deve ter pelo menos 6 dígitos");
        }
        if(!containsUpperCase(password)){
            throw new PasswordRulesException("Senha deve ter pelo menos um caractere maiúsculo");
        }
        if(!containsLowerCase(password)){
            throw new PasswordRulesException("Senha deve ter pelo menos um caractere minúsculo");
        }
        if(!containsSpecialCharacter(password)){
            throw new PasswordRulesException("Senha deve ter pelo menos um caractere especial como: !@#$%&*");
        }
    }

    public static boolean containsSpecialCharacter(String password) {
        Pattern pattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }

    public static boolean containsUpperCase(String password) {
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsLowerCase(String password) {
        for (char ch : password.toCharArray()) {
            if (Character.isLowerCase(ch)) {
                return true;
            }
        }
        return false;
    }

    public void deleteKeyById(Long id) {
        Optional<Key> keyOptional = this.repository.findById(id);
        if (keyOptional.isPresent()) {
            String name = keyOptional.get().getName();
            this.repository.deleteById(id);
            s3Management.deleteObject(name+KeyManagementService.privateKeySufix);
            s3Management.deleteObject(name+KeyManagementService.publicKeySufix);
        }else{
            throw new KeyNotFoundException();
        }
    }

    public Key updateKey(Long id, KeyDTO keyDTO) {
        Optional<Key> keyOptional = this.repository.findById(id);
        if (keyOptional.isPresent()) {
            String oldName = keyOptional.get().getName();
            Key key = keyOptional.get();
            key.setName(keyDTO.name());
            key.setFileUrl(keyDTO.fileUrl());
            key.setPassword(keyDTO.password());
            Key updatedKey =  this.repository.save(key);
            s3Management.renameObject(oldName+KeyManagementService.privateKeySufix, key.getName()+KeyManagementService.privateKeySufix);
            s3Management.renameObject(oldName+KeyManagementService.publicKeySufix, key.getName()+KeyManagementService.publicKeySufix);
            return updatedKey;
        } else {
            return null;
        }
    }
}
