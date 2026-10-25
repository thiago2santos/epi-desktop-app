package br.com.easynr6.gestaoepi.ui;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

/** Classes da folha enr6.css. Telas de negocio nao usam setStyle. */
public final class Enr6Styles {

  public static final String PAGE = "enr6-page";
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
  public static final String FORM = "enr6-form";
  public static final String METRIC = "enr6-metric";
  public static final String METRIC_VALUE = "enr6-metric-value";
  public static final String LEGAL_BAR = "enr6-legal-bar";
  public static final String SHEET = "enr6-sheet";
  public static final String SHEET_KICKER = "enr6-sheet-kicker";
  public static final String SHEET_NAME = "enr6-sheet-name";
  public static final String SHEET_META = "enr6-sheet-meta";
  public static final String SHEET_BODY = "enr6-sheet-body";
  public static final String SHEET_SIGN_LINE = "enr6-sheet-sign-line";
  public static final String SHEET_TABLE = "enr6-sheet-table";
  public static final String SHEET_HEAD = "enr6-sheet-head";
  public static final String SHEET_CELL = "enr6-sheet-cell";
  public static final String SHEET_CELL_NUM = "enr6-sheet-cell-num";
  public static final String SHEET_NOTE = "enr6-sheet-note";
  public static final String SHEET_REVIEW = "enr6-sheet-review";
  public static final String SHEET_GROUP = "enr6-sheet-group";
  public static final String SHEET_PROOF = "enr6-sheet-proof";
  public static final String SHEET_DOC_TITLE = "enr6-sheet-doc-title";
  public static final String SHEET_GRID = "enr6-sheet-grid";
  public static final String SHEET_FIELD = "enr6-sheet-field";
  public static final String SHEET_FIELD_HEAD = "enr6-sheet-field-head";
  public static final String SHEET_FIELD_LABEL = "enr6-sheet-field-label";
  public static final String SHEET_FIELD_VALUE = "enr6-sheet-field-value";
  public static final String PAGINATION = "enr6-pagination";
  public static final String ROW_INATIVO = "enr6-row-inativo";
  public static final String ROW_FORA = "enr6-row-fora";
  public static final String OPCAO_DESLIGADA = "enr6-opcao-desligada";
  public static final String STEP = "enr6-step";
  public static final String STEP_INDEX = "enr6-step-index";
  public static final String STEP_TITLE = "enr6-step-title";
  public static final String STEP_DONE = "enr6-step-done";
  public static final String STEP_CURRENT = "enr6-step-current";
  public static final String STEP_LINE = "enr6-step-line";
  public static final String STEP_LINE_DONE = "enr6-step-line-done";
  public static final String ICON_BUTTON = "enr6-icon-button";
  public static final String GLYPH = "enr6-glyph";

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
