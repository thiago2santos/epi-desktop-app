package br.com.easynr6.gestaoepi.ui;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

/** Classes da folha enr6.css. Telas de negocio nao usam setStyle. */
public final class Enr6Styles {

  public static final String PAGE_TITLE = "enr6-page-title";
  public static final String PAGE_DESCRIPTION = "enr6-page-description";
  public static final String FEEDBACK_OK = "enr6-feedback-ok";
  public static final String FEEDBACK_DANGER = "enr6-feedback-danger";
  public static final String FEEDBACK_WARN = "enr6-feedback-warn";
  public static final String EMPHASIS = "enr6-emphasis";
  public static final String FIELD_INVALID = "enr6-field-invalid";
  public static final String NAV_ITEM = "enr6-nav-item";
  public static final String NAV_GROUP_HEAD = "enr6-nav-group-head";
  public static final String NAV_CHEVRON = "enr6-nav-chevron";
  public static final String SHELL_SIDEBAR = "enr6-shell-sidebar";
  public static final String SHELL_SIDEBAR_TITLE = "enr6-shell-sidebar-title";
  public static final String SHELL_HEADER = "enr6-shell-header";
  public static final String SHELL_BRAND = "enr6-shell-brand";
  public static final String SHELL_META = "enr6-shell-meta";
  public static final String LOGIN_ROOT = "enr6-login-root";
  public static final String LOGIN_CARD = "enr6-login-card";
  public static final String LOGIN_TITLE = "enr6-login-title";
  public static final String LOGIN_SUBTITLE = "enr6-login-subtitle";
  public static final String PLACEHOLDER = "enr6-placeholder";
  public static final String NAV_ITEM_ACTIVE = "enr6-nav-item-active";
  public static final String STATUS_STRIP = "enr6-status-strip";
  public static final String UC_TAG = "enr6-uc-tag";
  public static final String BANNER_INFO = "enr6-banner-info";
  public static final String PANEL = "enr6-panel";
  public static final String METRIC = "enr6-metric";
  public static final String METRIC_VALUE = "enr6-metric-value";
  public static final String LEGAL_BAR = "enr6-legal-bar";

  private Enr6Styles() {}

  public static void markOk(Label label) {
    swap(label, FEEDBACK_OK, FEEDBACK_DANGER, FEEDBACK_WARN);
  }

  public static void markDanger(Label label) {
    swap(label, FEEDBACK_DANGER, FEEDBACK_OK, FEEDBACK_WARN);
  }

  public static void markWarn(Label label) {
    swap(label, FEEDBACK_WARN, FEEDBACK_OK, FEEDBACK_DANGER);
  }

  public static void markFieldInvalid(TextInputControl field, boolean invalid) {
    if (invalid) {
      if (!field.getStyleClass().contains(FIELD_INVALID)) {
        field.getStyleClass().add(FIELD_INVALID);
      }
      return;
    }
    field.getStyleClass().remove(FIELD_INVALID);
  }

  private static void swap(Label label, String add, String... remove) {
    label.getStyleClass().removeAll(remove);
    if (!label.getStyleClass().contains(add)) {
      label.getStyleClass().add(add);
    }
  }
}
