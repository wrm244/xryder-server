package cn.xryder.base.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Set;

/**
 * @author : JoeTao
 * createAt: 2018/9/14
 */
@Builder
@Data
public class LoginUser implements UserDetails {
    @Serial
    private static final long serialVersionUID = 6013032489896333195L;

    private String username;
    private String name;
    private String password;
    private boolean enabled;
    private String token;
    private String refreshToken;
    private Set<GrantedAuthority> authorities;

    @Override
    public Set<GrantedAuthority>  getAuthorities() {
        return this.authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
