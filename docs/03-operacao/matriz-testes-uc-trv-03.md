# Matriz executavel de testes - UC-TRV-03 (PDF)

Spec: `docs/03-operacao/spec-uc-trv-03-pdf.md`.

| ID | Tipo | Referencia | Pre-condicoes | Passos | Resultado esperado |
|---|---|---|---|---|---|
| TRV03-001 | INT | CA-01 | Previa da ficha com duas linhas | Salvar o PDF | Arquivo com as duas linhas, instante e operador |
| TRV03-002 | UI | CA-02 | Dialogo de arquivo aberto | Cancelar | Sem arquivo e sem auditoria de sucesso |
| TRV03-003 | INT | CA-03, `REL-001` | Falha forcada na escrita | Gerar | Sem arquivo parcial; "Nao foi possivel gerar o PDF. Nada foi salvo." |
| TRV03-004 | UI | CA-04 | Geracao lenta | Navegar o menu durante a fase | A janela responde |
| TRV03-005 | RBAC | CA-05 | `ALMOXARIFE` | Exportar cobertura e budget | Cobertura gera; budget recusa |
| TRV03-006 | AUDIT | CA-06 | Exportacao de cobertura | Gerar | `RELATORIO_EXPORTADO` com identificador `COBERTURA` e os filtros |
