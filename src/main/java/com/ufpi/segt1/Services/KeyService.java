package com.ufpi.segt1.Services;

import com.ufpi.segt1.DTO.KeyDTO;
import com.ufpi.segt1.Exceptions.FieldAlreadyInUseException;
import com.ufpi.segt1.Exceptions.KeyNotFoundException;
import com.ufpi.segt1.Exceptions.PasswordRulesException;
import com.ufpi.segt1.Models.Key;
import com.ufpi.segt1.Repositories.KeyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class KeyService {
    private final KeyRepository repository;
    private static final String AES_ALGORITHM = "AES";
    private static final String KEY = "KEY_DE_TESTE";

    public KeyService(KeyRepository repository) {
        this.repository = repository;
    }

    public List<Key> getAllKeys() {
        return this.repository.findAll();
    }

    public Key findKeyById(Long id){
        return repository.findKeyById(id).orElseThrow(KeyNotFoundException::new);
    }
    public Key createKey(KeyDTO keyDTO){
        Key newKey = new Key(keyDTO);
        this.SaveKey(newKey);
        return newKey;
    }

    private void SaveKey(Key key){
        ValidatePassword(key.getPassword());
        try {
            key.setPassword(encrypt(key.getPassword()));
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

    public static String encrypt(String plaintext) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = Arrays.copyOf(KEY.getBytes(), 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public void deleteKeyById(Long id) {
        Optional<Key> keyOptional = this.repository.findById(id);
        if (keyOptional.isPresent()) {
            this.repository.deleteById(id);
        }else{
            throw new KeyNotFoundException();
        }
    }

    public Key updateKey(Long id, KeyDTO keyDTO) {
        Optional<Key> keyOptional = this.repository.findById(id);
        if (keyOptional.isPresent()) {
            Key key = keyOptional.get();
            key.setId(keyDTO.id());
            key.setName(keyDTO.name());
            key.setPub(keyDTO.pub());
            key.setPriv(keyDTO.priv());
            key.setPassword(keyDTO.password());
            return this.repository.save(key);
        } else {
            return null;
        }
    }
}
