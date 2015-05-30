package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value=FrontendAPIRestController.FRONTEND_URL)
// restricted by AppsecretChecker: @Secured({"ROLE_USER"})
public class FrontendAPIRestController {

	public static final String FRONTEND_URL = "/frontend";
	public static final String FRONTEND_URL_PATTERN = FRONTEND_URL + "/**";

	@Autowired
	private UserRepository userRepository;

    @Autowired
	private UserService userService;


    // @RolesAllowed("ROLE_USER")
    // @RolesAllowed(value = { "USER" })
    // @PreAuthorize("hasAuthority('ROLE_USER')")
	@RequestMapping(value="registerUser", method= RequestMethod.GET )
	public String registerUser(@RequestParam("username") String username,
                               @RequestParam("pin") String pin,
                               @RequestParam("installationId") String installationId)
    throws Exception {

		final User user = userService.registerOrUpdateUser(username, pin, installationId);

        return "saved:"
                + (user.isNewUser() ? " new" : " changed")
                + (user.isActive() ? " active": " inactive");
	}
}
