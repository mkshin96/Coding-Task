package me.mugon.lendit.web;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {

    @GetMapping("/api")
    public RepresentationModel index() {
        RepresentationModel index = new RepresentationModel();
        index.add(linkTo(LoginController.class).withRel("login"));
        index.add(new Link("https://mkshin96.github.io/Coding-Task/#resources-index-access").withRel("profile"));
        return index;
    }
}
