package com.ufpi.segt1.Services;

import com.ufpi.segt1.DTO.LoginDTO;
import com.ufpi.segt1.DTO.UserDTO;
import com.ufpi.segt1.Exceptions.*;
import com.ufpi.segt1.Models.User;
import com.ufpi.segt1.Repositories.UserRepository;
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
public class UserService {
    private final UserRepository repository;
    private static final String AES_ALGORITHM = "AES";
    private static final String KEY = "KEY_DE_TESTE";

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User findUserById(Long id){
        return repository.findUserById(id).orElseThrow(UserNotFoundException::new);
    }

    public User findUserByEmail(String email){
        return repository.findUserByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    public User createUser(UserDTO userDTO){
        User newUser = new User(userDTO);
        this.SaveUser(newUser);
        return newUser;
    }

    private void SaveUser(User user){
        ValidateUser(user);
        try{
            user.setPassword(encrypt(user.getPassword()));
            this.repository.save(user);
        }catch (DataIntegrityViolationException ex){
            String constrainField = GeneralService.getConstrainField(ex);
            throw new FieldAlreadyInUseException(constrainField);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void ValidateUser(User user){
        ValidateEmail(user.getEmail());
        ValidatePassword(user.getPassword());
    }

    private void ValidateEmail(String email){
        if(!email.contains("@")){
            throw new InvalidEmailException("Insira um email válido");
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

    public User Login(LoginDTO loginDTO){
        try {
            User user = findUserByEmail(loginDTO.email());
            if(encrypt(loginDTO.password()).equals(user.getPassword())){
                return user;
            }else{
                throw new LoginIncorrectDataException();
            }
        } catch (Exception e) {
            throw new LoginIncorrectDataException();
        }
    }

    public List<User> getAllUsers() {
        return this.repository.findAll();
    }

    public static String encrypt(String plaintext) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = Arrays.copyOf(KEY.getBytes(), 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public void deleteUserById(Long id) {
        Optional<User> userOptional = this.repository.findById(id);
        if (userOptional.isPresent()) {
            this.repository.deleteById(id);
        }
    }

    public User updateUser(Long id, UserDTO userDTO) {
        Optional<User> userOptional = this.repository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setName(userDTO.name());
            user.setUsername(userDTO.username());
            user.setEmail(userDTO.email());
            return this.repository.save(user);
        } else {
            return null;
        }
    }
}
