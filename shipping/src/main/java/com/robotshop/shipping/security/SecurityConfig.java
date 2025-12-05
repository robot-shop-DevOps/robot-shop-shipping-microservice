@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf().disable()
                .authorizeHttpRequests()
                // ALLOW HEALTH ENDPOINTS WITHOUT TOKEN
                .requestMatchers("/health/**").permitAll()
                
                // REQUIRE TOKEN FOR EVERYTHING ELSE
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}