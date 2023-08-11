package com.coding404.demo.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.coding404.demo.user.MyUserDetailService;

@Configuration //설정파일
@EnableWebSecurity //이 설정파일을 시큐리티 필터에 추가
@EnableGlobalMethodSecurity(prePostEnabled = true) //어노테이션으로 권한을 지정할 수 있게 함
public class SecurityConfig {
	
	//나를 기억해에서 사용할 UserDetailService
	@Autowired
	private MyUserDetailService myUserDetailServcie;
	
	//비밀번호 암화화객체
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	
	@Bean
	public SecurityFilterChain securityFilter(HttpSecurity http) throws Exception {
		
		//csrf 토큰 X / http는 builder패턴
		http.csrf().disable();
		
		//권한 설정
		//http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
		
		//모든 페이지에 대해서 거부
		//http.authorizeHttpRequests(authorize -> authorize.anyRequest().denyAll());
		
		//user페이지에 대해서 인증이 필요
		//http.authorizeHttpRequests(authorize -> authorize.antMatchers("/user/**").authenticated());
		
		
		//user페이지에 대해서 권한이 필요
		//http.authorizeHttpRequests(authorize -> authorize.antMatchers("/user/**").hasRole("USER"));
		
		//user페이지는 user권한이 필요, admin페이지는 admin 권한이 필요
//		http.authorizeHttpRequests(authorize -> authorize.antMatchers("/user/**").hasRole("USER")
//														 .antMatchers("/admin/**").hasRole("ADMIN"));
//		
//		http.authorizeHttpRequests(authorize -> authorize.antMatchers("/all").authenticated() //all 페이지는 인증이 필요
//														  .antMatchers("/user/**").hasRole("USER") //user페이지는 USER권한이 필요
//														  .antMatchers("/admin/**").hasRole("ADMIN") //admin페이지는 ADMIN권한이 필요
//														  .anyRequest().permitAll()); //위 페이지 이외의 페이지는 접근이 가능함
		
		
		// 권한 앞에는 ROLE_가 자동으로 생략이 됨
		http.authorizeHttpRequests(authorize -> authorize.antMatchers("/all").authenticated() //all 페이지는 인증이 필요
				.antMatchers("/user/**").hasAnyRole("USER", "ADMIN", "TESTER") //user페이지는 USER, ADMIN, TESTER 중 하나의 권한이 필요
				.antMatchers("/admin/**").hasRole("ADMIN") //admin페이지는 ADMIN권한이 필요
				.anyRequest().permitAll()); //위 페이지 이외의 페이지는 접근이 가능함
		
		
		//시큐리티 설정파일 만들면, 시큐리티가 제공하는 기본 로그인페이지가 보이지 않게됨
		//시큐리티가 사용하는 기본로그인페이지를 사용함
		//권한 or 인증이 되지 않으면 기본으로 선언된 로그인패이지를 보게주게 됨
		http.formLogin(Customizer.withDefaults()); //기본로그인페이지사용
		
		//사용자가 제공하는 폼기반 로그인 기능을 사용할 수 있음
		http.formLogin()
			.loginPage("/login") //login 화면
			.loginProcessingUrl("/loginForm")//로그인 시도 요청경로 -> 스프링이 로그인 시도를 낚아채서 UserDetailService객체로 연결	
			.defaultSuccessUrl("/all")//로그인 성공 시 화면
			.failureUrl("/login?err=true")
			.and() //다시 http 객체 사용가능
			.exceptionHandling().accessDeniedPage("/deny")//권한이 없을때 이동할 리다이렉트 경로
			.and()
			.logout().logoutUrl("/logout").logoutSuccessUrl("/hello"); //default로그아웃 경로/logout, /logout 주소를 직접 작성할 수 있고 로그아웃 성공시 리다이렉트할 경로
		
		
		// 나를 기억해
		http.rememberMe()
		    .key("coding404") //토큰(쿠키)를 만들 비밀키 (필수)
		    .rememberMeParameter("remember-me") //화면에서 전달받는 checked name명(필수)
		    .tokenValiditySeconds(60) //쿠키(토큰)의 유효시간(필수)
		    .userDetailsService(myUserDetailServcie)//토큰이 있을 때 실행시킬 userDetailService 객체(필수)
		    .authenticationSuccessHandler(customRemeberMe()); //나를 기억해가 동작할 때 실행할 핸들러 객체를 넣음
		return http.build();
	}
	
	//customRememberMe
	@Bean
	public CustomRememberMe customRemeberMe() {
		CustomRememberMe me = new CustomRememberMe("/all"); //리멤버미 성공시 실행시킬 리다이렉트 주소
		return me;
	}
}
