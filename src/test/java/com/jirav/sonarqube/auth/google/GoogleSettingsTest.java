/*
 * Google Authentication for SonarQube
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.jirav.sonarqube.auth.google;

import org.junit.Test;
import org.sonar.api.config.Settings;

import static org.assertj.core.api.Assertions.assertThat;
import static com.jirav.sonarqube.auth.google.GoogleSettings.LOGIN_STRATEGY_DEFAULT_VALUE;
import static com.jirav.sonarqube.auth.google.GoogleSettings.LOGIN_STRATEGY_PROVIDER_ID;

public class GoogleSettingsTest {

  Settings settings = new Settings();

  GoogleSettings underTest = new GoogleSettings(settings);

  @Test
  public void is_enabled() throws Exception {
    settings.setProperty("sonar.auth.google.clientId.secured", "id");
    settings.setProperty("sonar.auth.google.clientSecret.secured", "secret");
    settings.setProperty("sonar.auth.google.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);

    settings.setProperty("sonar.auth.google.enabled", true);
    assertThat(underTest.isEnabled()).isTrue();

    settings.setProperty("sonar.auth.google.enabled", false);
    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void is_enabled_always_return_false_when_client_id_is_null() throws Exception {
    settings.setProperty("sonar.auth.google.enabled", true);
    settings.setProperty("sonar.auth.google.clientId.secured", (String) null);
    settings.setProperty("sonar.auth.google.clientSecret.secured", "secret");
    settings.setProperty("sonar.auth.google.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);

    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void is_enabled_always_return_false_when_client_secret_is_null() throws Exception {
    settings.setProperty("sonar.auth.google.enabled", true);
    settings.setProperty("sonar.auth.google.clientId.secured", "id");
    settings.setProperty("sonar.auth.google.clientSecret.secured", (String) null);
    settings.setProperty("sonar.auth.google.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);

    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void is_enabled_always_return_false_when_login_strategy_is_null() throws Exception {
    settings.setProperty("sonar.auth.google.enabled", true);
    settings.setProperty("sonar.auth.google.clientId.secured", "id");
    settings.setProperty("sonar.auth.google.clientSecret.secured", "secret");
    settings.setProperty("sonar.auth.google.loginStrategy", (String) null);

    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void return_client_id() throws Exception {
    settings.setProperty("sonar.auth.google.clientId.secured", "id");
    assertThat(underTest.clientId()).isEqualTo("id");
  }

  @Test
  public void return_client_secret() throws Exception {
    settings.setProperty("sonar.auth.google.clientSecret.secured", "secret");
    assertThat(underTest.clientSecret()).isEqualTo("secret");
  }

  @Test
  public void return_login_strategy() throws Exception {
    settings.setProperty("sonar.auth.google.loginStrategy", LOGIN_STRATEGY_PROVIDER_ID);
    assertThat(underTest.loginStrategy()).isEqualTo(LOGIN_STRATEGY_PROVIDER_ID);
  }

  @Test
  public void allow_users_to_sign_up() throws Exception {
    settings.setProperty("sonar.auth.google.allowUsersToSignUp", "true");
    assertThat(underTest.allowUsersToSignUp()).isTrue();

    settings.setProperty("sonar.auth.google.allowUsersToSignUp", "false");
    assertThat(underTest.allowUsersToSignUp()).isFalse();
  }

  @Test
  public void definitions() throws Exception {
    assertThat(GoogleSettings.definitions()).hasSize(5);
  }
}
