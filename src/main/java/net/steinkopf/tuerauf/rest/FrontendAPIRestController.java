package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/frontend")
public class FrontendAPIRestController {

	@Autowired
	private UserRepository userRepository;

    @Autowired
	private UserService userService;


    // @RolesAllowed("ROLE_USER")
    // @RolesAllowed(value = { "USER" })
    // @PreAuthorize("hasAuthority('ROLE_USER')")
    // @Secured({"ROLE_ADMIN"})
	@RequestMapping(value="registerUser", method= RequestMethod.GET )
	public String registerUser(@RequestParam("username") String username,
                               @RequestParam("pin") String pin,
                               @RequestParam("installationId") String installationId) {

		return userService.registerUser(username, pin, installationId);
	}
}
