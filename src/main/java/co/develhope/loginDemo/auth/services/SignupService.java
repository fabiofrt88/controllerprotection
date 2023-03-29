package co.develhope.loginDemo.auth.services;

import co.develhope.loginDemo.auth.entities.SignupActivationDTO;
import co.develhope.loginDemo.auth.entities.SignupDTO;
import co.develhope.loginDemo.notification.services.MailNotificationService;
import co.develhope.loginDemo.user.entities.Role;
import co.develhope.loginDemo.user.entities.User;
import co.develhope.loginDemo.user.repositories.RoleRepository;
import co.develhope.loginDemo.user.repositories.UserRepository;
import co.develhope.loginDemo.user.utils.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SignupService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void signUp(SignupDTO signupDTO) throws Exception {
        User userInDB = userRepository.findByEmail(signupDTO.getEmail());
        if(userInDB != null) throw new Exception("User already exists");
        User user = new User();
        user.setName(signupDTO.getName());
        user.setEmail(signupDTO.getEmail());
        user.setSurname(signupDTO.getSurname());
        user.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        user.setActivationCode(UUID.randomUUID().toString());

        Set<Role> roles = new HashSet<>();
        Optional<Role> userRole = Optional.ofNullable(roleRepository.findByName(Roles.REGISTERED));
        if(userRole.isEmpty()) throw new Exception("Cannot set the role");
        roles.add(userRole.get());
        user.setRoles(roles);
        mailNotificationService.sendActivationEmail(user);
        userRepository.save(user);
    }

    public void activate(SignupActivationDTO signupActivationDTO) throws Exception {
        User user = userRepository.findByActivationCode(signupActivationDTO.getActivationCode());
        if(user == null) throw new Exception("User not found");
        user.setActive(true);
        user.setActivationCode(null);
        userRepository.save(user);
    }
}