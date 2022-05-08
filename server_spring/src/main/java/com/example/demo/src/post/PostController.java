package com.example.demo.src.post;

import com.example.demo.src.user.UserProvider;
import com.example.demo.src.user.UserService;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PostProvider userProvider;
    @Autowired
    private final PostService userService;
    @Autowired
    private final JwtService jwtService;

    // command + n
    public PostController(PostProvider userProvider, PostService userService, JwtService jwtService) {
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }



}
