package me.mugon.lendit.web;

import me.mugon.lendit.common.BaseControllerTest;
import org.junit.jupiter.api.Test;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IndexControlerTest extends BaseControllerTest {

    @Test
    void index() throws Exception {
        mockMvc.perform(get("/api"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.login").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("index",
                        links(
                                linkWithRel("login").description("link to login"),
                                linkWithRel("profile").description("link to profile")
                        )
                ));
    }
}
