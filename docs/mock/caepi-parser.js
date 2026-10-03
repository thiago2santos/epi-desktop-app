/**
 * UC-CAE-01 — parsers de carga manual (protótipo).
 * Formatos:
 *  - TGG: tgg_export_caepi.txt (| , ~19 colunas) dentro do ZIP oficial FTP.
 *  - RELATORIO CSV: exportação do portal (ConsultaCAInternet / RelatorioCA_*.csv), ; + UTF-8 BOM.
 */
(function (global) {
  const STATUS_RANK = { ACTIVE: 4, SUSPENDED: 3, CANCELED: 2, EXPIRED: 1, UNKNOWN: 0 };

  function stripBom(text) {
    if (!text) return text;
    return text.charCodeAt(0) === 0xfeff ? text.slice(1) : text.replace(/^\uFEFF/, '');
  }

  function mapStatus(raw) {
    const u = (raw || '').trim().toUpperCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    if (/CANCEL/.test(u)) return 'CANCELED';
    if (/SUSPEND/.test(u)) return 'SUSPENDED';
    if (/VENCID|EXPIR/.test(u)) return 'EXPIRED';
    if (/VALID/.test(u)) return 'ACTIVE';
    if (/ATIV/.test(u)) return 'ACTIVE';
    return u ? 'UNKNOWN' : 'UNKNOWN';
  }

  function normalizeCa(s) {
    const d = String(s || '').replace(/\D/g, '');
    return d.length >= 1 && d.length <= 6 ? d.replace(/^0+/, '') || d : null;
  }

  function parseDateBr(s) {
    const m = (s || '').trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (!m) return null;
    return m[3] + '-' + m[2] + '-' + m[1];
  }

  function detectFormat(text) {
    const head = stripBom(text).slice(0, 800).toUpperCase();
    if (head.indexOf('NR REGISTRO CA') >= 0 && head.indexOf(';') >= 0) return 'CAEPI_RELATORIO_CSV';
    if (text.indexOf('|') >= 0 && /CAEPI|TGG|REGISTRO/i.test(head)) return 'TGG_PIPE';
    if (head.indexOf('NR REGISTRO CA') >= 0) return 'CAEPI_RELATORIO_CSV';
    return 'TGG_PIPE';
  }

  /**
   * CSV ; com campos entre aspas (portal CAEPI).
   * Não materializa todas as linhas — evita OOM em RelatorioCA (~120k+ variantes).
   */
  function forEachCsvRow(text, onRow) {
    let row = [];
    let cell = '';
    let inQuotes = false;
    const s = stripBom(text);
    let rowIndex = 0;
    function flushRow() {
      row.push(cell);
      cell = '';
      const hasData = row.some(c => c && c.trim());
      if (hasData) onRow(rowIndex++, row);
      row = [];
    }
    for (let i = 0; i < s.length; i++) {
      const c = s[i];
      if (inQuotes) {
        if (c === '"') {
          if (s[i + 1] === '"') {
            cell += '"';
            i++;
          } else inQuotes = false;
        } else cell += c;
      } else if (c === '"') {
        inQuotes = true;
      } else if (c === ';') {
        row.push(cell);
        cell = '';
      } else if (c === '\r') {
        /* ignore */
      } else if (c === '\n') {
        flushRow();
      } else cell += c;
    }
    if (cell.length || row.length) flushRow();
  }

  function normHeader(h) {
    return (h || '').trim().toUpperCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }

  function mergeIntoIndex(index, entry) {
    const ca = entry.caNumber;
    const prev = index[ca];
    if (!prev) {
      index[ca] = entry;
      return;
    }
    const rNew = STATUS_RANK[entry.status] || 0;
    const rOld = STATUS_RANK[prev.status] || 0;
    if (rNew > rOld) {
      index[ca] = entry;
      return;
    }
    if (rNew === rOld && entry.validUntil && prev.validUntil && entry.validUntil > prev.validUntil) {
      index[ca] = entry;
    }
  }

  function parseRelatorioCsv(text, options) {
    options = options || {};
    const maxRows = options.maxRows || 500000;
    const onProgress = options.onProgress;
    let headerMap = null;
    let idxCa = idxSit = idxVal = idxEq = idxFab = -1;
    const index = {};
    let skipped = 0;
    let used = 0;
    let dataRows = 0;

    forEachCsvRow(text, (rowIdx, cols) => {
      if (rowIdx === 0) {
        const header = cols.map(normHeader);
        idxCa = header.findIndex(h => h.indexOf('REGISTRO CA') >= 0);
        idxSit = header.findIndex(h => h.indexOf('SITUACAO') >= 0);
        idxVal = header.findIndex(h => h.indexOf('VALIDADE') >= 0);
        idxEq = header.findIndex(h => h === 'EQUIPAMENTO');
        idxFab = header.findIndex(
          h => h.indexOf('RAZAO SOCIAL') >= 0 && h.indexOf('LABORATORIO') < 0
        );
        headerMap = header;
        if (idxCa < 0 || idxSit < 0) {
          throw new Error('CAE-003 Cabeçalho RelatorioCA não reconhecido (esperado NR Registro CA; SITUACAO).');
        }
        return;
      }
      if (used >= maxRows) return;
      dataRows++;
      const ca = normalizeCa((cols[idxCa] || '').replace(/"/g, ''));
      if (!ca) {
        skipped++;
        return;
      }
      const situacaoRaw = (cols[idxSit] || '').trim();
      mergeIntoIndex(index, {
        caNumber: ca,
        status: mapStatus(situacaoRaw),
        statusLabel: situacaoRaw || '—',
        validUntil: idxVal >= 0 ? parseDateBr(cols[idxVal]) : null,
        equipment: (idxEq >= 0 ? cols[idxEq] : '').trim().slice(0, 160),
        manufacturer: (idxFab >= 0 ? cols[idxFab] : '').trim().slice(0, 80),
        source: 'CAEPI_RELATORIO_CSV',
      });
      used++;
      if (onProgress && used % 10000 === 0) onProgress({ phase: 'parse', lines: used, total: null });
    });

    if (!headerMap) {
      return { index: {}, lineCount: 0, recordCount: 0, skipped: 0, format: 'CAEPI_RELATORIO_CSV' };
    }
    return {
      index,
      lineCount: dataRows,
      recordCount: Object.keys(index).length,
      skipped,
      truncated: used >= maxRows,
      format: 'CAEPI_RELATORIO_CSV',
      variantRows: used,
    };
  }

  function parseTggPipe(text, options) {
    options = options || {};
    const maxLines = options.maxLines || 200000;
    const onProgress = options.onProgress;
    const index = {};
    let lineCount = 0;
    let used = 0;
    let skipped = 0;
    const lines = stripBom(text).split(/\r?\n/);
    for (let i = 0; i < lines.length && used < maxLines; i++) {
      const line = lines[i].trim();
      if (!line) continue;
      lineCount++;
      if (/^nr|^numero|^registro/i.test(line) && i === 0) continue;
      const cols = line.split('|');
      if (cols.length < 5) {
        skipped++;
        continue;
      }
      let ca = normalizeCa(cols[0]);
      if (!ca) {
        for (let c = 0; c < Math.min(cols.length, 4); c++) {
          ca = normalizeCa(cols[c]);
          if (ca) break;
        }
      }
      if (!ca) {
        skipped++;
        continue;
      }
      let situacaoRaw = '';
      for (let c = cols.length - 1; c >= 0; c--) {
        const cell = cols[c].trim();
        if (/ATIV|CANCEL|SUSP|VENC|VALID/i.test(cell)) {
          situacaoRaw = cell;
          break;
        }
      }
      if (!situacaoRaw && cols[6]) situacaoRaw = cols[6];
      mergeIntoIndex(index, {
        caNumber: ca,
        status: mapStatus(situacaoRaw),
        statusLabel: situacaoRaw || '—',
        equipment: (cols[7] || cols[3] || cols[2] || '').trim().slice(0, 160),
        source: 'TGG_PIPE',
      });
      used++;
      if (onProgress && used % 5000 === 0) onProgress({ phase: 'parse', lines: used, total: lines.length });
    }
    return {
      index,
      lineCount,
      recordCount: Object.keys(index).length,
      skipped,
      truncated: used >= maxLines,
      format: 'TGG_PIPE',
      variantRows: used,
    };
  }

  function parseText(text, options) {
    const fmt = detectFormat(text);
    const result = fmt === 'CAEPI_RELATORIO_CSV'
      ? parseRelatorioCsv(text, options)
      : parseTggPipe(text, options);
    if (result.recordCount < 1) {
      throw new Error('CAE-003 Nenhum CA reconhecido no arquivo.');
    }
    return result;
  }

  global.Enr6CaepiParser = { parseText, detectFormat, parseRelatorioCsv, parseTggPipe };
})(typeof window !== 'undefined' ? window : globalThis);
