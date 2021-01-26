package de.rwth.idsg.steve.web.controller;


import javax.validation.Valid;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.rwth.idsg.steve.repository.dto.User;
import de.rwth.idsg.steve.utils.ControllerHelper;
import de.rwth.idsg.steve.web.dto.UserForm;
import de.rwth.idsg.steve.web.dto.UserQueryForm;
import de.rwth.idsg.steve.web.dto.UserSex;
import net.parkl.ocpp.service.cs.OcppIdTagService;
import net.parkl.ocpp.service.cs.UserService;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 25.11.2015
 */
@Controller
@RequestMapping(value = "/manager/users")
public class UsersController {

    @Autowired private OcppIdTagService ocppTagService;
    @Autowired private UserService userService;

    private static final String PARAMS = "params";

    // -------------------------------------------------------------------------
    // Paths
    // -------------------------------------------------------------------------

    private static final String QUERY_PATH = "/query";

    private static final String DETAILS_PATH = "/details/{userPk}";
    private static final String DELETE_PATH = "/delete/{userPk}";
    private static final String UPDATE_PATH = "/update";
    private static final String ADD_PATH = "/add";

    // -------------------------------------------------------------------------
    // HTTP methods
    // -------------------------------------------------------------------------

    @RequestMapping(method = RequestMethod.GET)
    public String getOverview(Model model) {
        initList(model, new UserQueryForm());
        return "data-man/users";
    }

    @RequestMapping(value = QUERY_PATH, method = RequestMethod.GET)
    public String getQuery(@ModelAttribute(PARAMS) UserQueryForm params, Model model) {
        initList(model, params);
        return "data-man/users";
    }

    private void initList(Model model, UserQueryForm params) {
        model.addAttribute(PARAMS, params);
        model.addAttribute("userList", userService.getOverview(params));
    }

    @RequestMapping(value = DETAILS_PATH, method = RequestMethod.GET)
    public String getDetails(@PathVariable("userPk") int userPk, Model model) {
        User.Details details = userService.getDetails(userPk);

        UserForm form = new UserForm();
        form.setUserPk(details.getUserRecord().getUserPk());
        form.setFirstName(details.getUserRecord().getFirstName());
        form.setLastName(details.getUserRecord().getLastName());
        if (details.getUserRecord().getBirthDay()!=null) {
        	form.setBirthDay(new DateTime(details.getUserRecord().getBirthDay()).toLocalDate());
        }
        form.setPhone(details.getUserRecord().getPhone());
        form.setSex(UserSex.fromDatabaseValue(details.getUserRecord().getSex()));
        form.setEMail(details.getUserRecord().getEmail());
        form.setNote(details.getUserRecord().getNote());
        form.setAddress(ControllerHelper.recordToDto(details.getAddress()));
        form.setOcppIdTag(details.getOcppIdTag().orElse(ControllerHelper.EMPTY_OPTION));

        model.addAttribute("userForm", form);
        setTags(model);
        return "data-man/userDetails";
    }

    @RequestMapping(value = ADD_PATH, method = RequestMethod.GET)
    public String addGet(Model model) {
        setTags(model);
        model.addAttribute("userForm", new UserForm());
        return "data-man/userAdd";
    }

    @RequestMapping(params = "add", value = ADD_PATH, method = RequestMethod.POST)
    public String addPost(@Valid @ModelAttribute("userForm") UserForm userForm,
                          BindingResult result, Model model) {
        if (result.hasErrors()) {
            setTags(model);
            return "data-man/userAdd";
        }

        userService.add(userForm);
        return toOverview();
    }

    @RequestMapping(params = "update", value = UPDATE_PATH, method = RequestMethod.POST)
    public String update(@Valid @ModelAttribute("userForm") UserForm userForm,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            setTags(model);
            return "data-man/userDetails";
        }

        userService.update(userForm);
        return toOverview();
    }

    @RequestMapping(value = DELETE_PATH, method = RequestMethod.POST)
    public String delete(@PathVariable("userPk") int userPk) {
        userService.delete(userPk);
        return toOverview();
    }

    private void setTags(Model model) {
        model.addAttribute("countryCodes", ControllerHelper.COUNTRY_DROPDOWN);
        model.addAttribute("idTagList", ControllerHelper.idTagEnhancer(ocppTagService.getIdTags()));
    }

    // -------------------------------------------------------------------------
    // Back to Overview
    // -------------------------------------------------------------------------

    @RequestMapping(params = "backToOverview", value = ADD_PATH, method = RequestMethod.POST)
    public String addBackToOverview() {
        return toOverview();
    }

    @RequestMapping(params = "backToOverview", value = UPDATE_PATH, method = RequestMethod.POST)
    public String updateBackToOverview() {
        return toOverview();
    }

    private String toOverview() {
        return "redirect:/manager/users";
    }
}
