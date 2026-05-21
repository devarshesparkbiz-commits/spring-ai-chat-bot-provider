package com.learn;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.SecretKey;

@SpringBootApplication
public class SpringAiChatBotProviderApplication {

	public static void main(String[] args) {
		SecretKey key = Jwts.SIG.HS256.key().build();

		String base64Secret =
				Encoders.BASE64.encode(key.getEncoded());

//		System.out.println(base64Secret);

		SpringApplication.run(SpringAiChatBotProviderApplication.class, args);
	}

}
