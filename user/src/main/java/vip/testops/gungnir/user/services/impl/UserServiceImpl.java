package vip.testops.gungnir.user.services.impl;

import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vip.testops.gungnir.user.commons.Response;
import vip.testops.gungnir.user.dao.LoginCacheRepository;
import vip.testops.gungnir.user.dao.UserRepository;
import vip.testops.gungnir.user.entities.dto.LoginCache;
import vip.testops.gungnir.user.entities.dto.UserDTO;
import vip.testops.gungnir.user.entities.vto.UserVTO;
import vip.testops.gungnir.user.services.UserService;
import vip.testops.gungnir.user.utils.DigestUtil;
import vip.testops.gungnir.user.utils.JWTUtil;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${token.expire}")
    private int tokenExpire;

    @Value("${jwt.key}")
    private String jwtKey;

    private UserRepository userRepository;
    private LoginCacheRepository loginCacheRepository;

    @Autowired
    public void setLoginCache(LoginCacheRepository loginCacheRepository) {
        this.loginCacheRepository = loginCacheRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void doSignIn(String email, String password, Response<UserVTO> response) {
        try {
            password = DigestUtil.digest(password, "SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("no such Algorithm.", e);
            response.serviceError("no such Algorithm");
            return;
        } catch (UnsupportedEncodingException e) {
            log.error("unsupported encoding.", e);
            response.serviceError("unsupported encoding");
            return;
        }
        UserDTO userDTO = userRepository.findByEmailAndPassword(email, password);
        if(userDTO == null) {
            response.serviceError("invalid email/password");
            return;
        }
        String token = generateToken(userDTO.getId(), userDTO.getName(), email);
        LoginCache loginCache = new LoginCache();
        loginCache.setUserId(userDTO.getId());
        loginCache.setToken(token);
        loginCache.setCreateTime(new Date());
        loginCache.setUpdateTime(new Date());
        loginCacheRepository.save(loginCache);

        UserVTO userVTO = new UserVTO();
        userVTO.setName(userDTO.getName());
        userVTO.setEmail(email);
        userVTO.setToken(token);
        response.dataSuccess(userVTO);
    }

    @Override
    public void doSignUp(String name, String email, String password, Response<UserVTO> response) {
        if(userRepository.existsByEmailOrName(name, email)) {
            response.serviceError("name/email is exists");
            return;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setName(name);
        userDTO.setEmail(email);
        try {
            password = DigestUtil.digest(password, "SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("no such Algorithm.", e);
            response.serviceError("no such Algorithm");
            return;
        } catch (UnsupportedEncodingException e) {
            log.error("unsupported encoding.", e);
            response.serviceError("unsupported encoding");
            return;
        }
        userDTO.setPassword(password);
        userDTO.setCreateTime(new Date());
        userDTO.setUpdateTime(new Date());
        userDTO = userRepository.save(userDTO);

        String token = generateToken(userDTO.getId(), name, email);
        LoginCache loginCache = new LoginCache();
        loginCache.setUserId(userDTO.getId());
        loginCache.setToken(token);
        loginCache.setCreateTime(new Date());
        loginCache.setUpdateTime(new Date());
        loginCacheRepository.save(loginCache);

        UserVTO userVTO = new UserVTO();
        userVTO.setName(name);
        userVTO.setEmail(email);
        userVTO.setToken(token);
        response.dataSuccess(userVTO);
    }

    @Override
    public void doAuthorize(String token, Response<UserDTO> response) {
        try {
            Claim claim = JWTUtil.verifyToken(token, JWTUtil.getSecret(jwtKey));
            Map<String, Object> userInfo = claim.asMap();
            String userId = userInfo.get("id").toString();
            LoginCache loginCache = loginCacheRepository.findByUserIdAndAndToken(userId, token);
            if(loginCache == null) {
                response.serviceError("token is invalid");
                return;
            }
            Optional<UserDTO> userDTOOptional = userRepository.findById(userId);
            if(!userDTOOptional.isPresent()) {
                response.serviceError("cannot find user");
                return;
            }
            response.dataSuccess(userDTOOptional.get());
        } catch (RuntimeException e) {
            response.serviceError("token is expired");
        }

    }

    private String generateToken(String id, String name, String email) {
        Map<String, Object> claim = new HashMap<>();
        claim.put("id", id);
        claim.put("name", name);
        claim.put("email", email);
        String secretKey = JWTUtil.getSecret(jwtKey);
        return JWTUtil.createToken(claim, secretKey, tokenExpire);
    }

}
