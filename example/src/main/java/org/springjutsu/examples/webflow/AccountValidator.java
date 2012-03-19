package org.springjutsu.examples.webflow;

import org.springframework.stereotype.Component;
import org.springjutsu.examples.entities.Account;
import org.springjutsu.validation.webflow.BaseWebflowValidator;

@Component("accountValidator")
public class AccountValidator extends BaseWebflowValidator<Account>{
	
}