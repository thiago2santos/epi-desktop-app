package br.com.easynr6.gestaoepi.modules.issuance.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.BaixaFornecimento;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcBaixaFornecimento implements BaixaFornecimento {

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcBaixaFornecimento(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void registrar(Baixa baixa) {
    jdbc.update(
        """
        INSERT INTO estoque_movimento (
            lote_id, movement_type, quantity, created_at,
            unit_cost_cents, unit_name, department_name, job_role_name, epi_id)
        VALUES (
            :loteId, 'BAIXA_FORNECIMENTO', :quantidade, :quando,
            :custo, :unidade, :setor, :funcao, :epiId)
        """,
        new MapSqlParameterSource()
            .addValue("loteId", baixa.loteId())
            .addValue("quantidade", baixa.quantidade())
            .addValue("quando", baixa.quando().toString())
            .addValue("custo", baixa.custoCentavos())
            .addValue("unidade", baixa.unidade())
            .addValue("setor", baixa.setor())
            .addValue("funcao", baixa.funcao())
            .addValue("epiId", baixa.epiId()));
  }
}
