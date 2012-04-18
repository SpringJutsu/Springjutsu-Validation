package org.springjutsu.examples.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springjutsu.examples.dao.AccountDao;
import org.springjutsu.examples.entities.Account;
import org.springjutsu.validation.mvc.annotations.SuccessView;
import org.springjutsu.validation.mvc.annotations.ValidationFailureView;

@Controller("accountController")
@RequestMapping("/accounts")
@SessionAttributes(types=Account.class)
public class AccountController {
	
	@Autowired
	private AccountDao accountDao;
	
	@RequestMapping(value="/new", method=RequestMethod.GET)
	@SuccessView("accounts/createForm")
	public Account createAccount() {
		return new Account();
	}
	
	@RequestMapping(value={"/{username}", "/{username}/edit"}, method=RequestMethod.GET)
	@SuccessView({"/{username}=accounts/view", "/{username}/edit=accounts/editForm"})
	public Account getAccount(@PathVariable("username") String username) {
		return accountDao.get(username);
	}
	
	@RequestMapping(value={"/new","/{username}/edit"}, method=RequestMethod.POST)
	@ValidationFailureView({"/new=accounts/createForm", "/{username}/edit=accounts/editForm"})
	@SuccessView("redirect:/accounts/{account.username}")
	public void saveAccount(@Valid Account account) {
		accountDao.save(account);
	}
	
}
