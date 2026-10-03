/**
 * Easy NR6 — banco mock em memória (cadastros + operação).
 * Persistência: IndexedDB (mock-idb.js) + window.__enr6Db no shell (iframe usa o pai).
 * Login mock continua em sessionStorage (enr6_mock_user).
 */
(function (global) {
  const LEGACY_STORAGE_KEY = 'enr6_mock_db_v1';
  /** Acima disso o índice CAEPI fica só no store `ca` (não embutido no snapshot app). */
  const CAEPI_IDB_THRESHOLD = 20;

  function todayStr() {
    const d = new Date();
    return d.toISOString().slice(0, 10);
  }

  function seedOperational() {
    return {
      caepi: {
        valid: true,
        batchId: 'CAEPI-20260928-928',
        loadedAt: '2026-09-28T03:12:00',
        recordCount: 12418,
        lastMode: 'AUTO',
        autoEnabled: true,
        lastAutoAttemptAt: '2026-09-28T03:00:02',
        lastAutoError: null,
        index: {
          '67890': { caNumber: '67890', status: 'ACTIVE', statusLabel: 'ATIVO', equipment: 'Protetor auricular plug' },
          '12345': { caNumber: '12345', status: 'ACTIVE', statusLabel: 'ATIVO', equipment: 'Óculos incolor' },
        },
        history: [
          { at: '2026-09-28T03:12:44', ok: true, durationMin: 12, mode: 'AUTO' },
          { at: '2026-09-27T03:11:20', ok: true, durationMin: 11, mode: 'AUTO' },
          { at: '2026-09-26T03:05:00', ok: false, durationMin: null, error: 'Falha rede', mode: 'AUTO' },
        ],
      },
      lots: [
        {
          id: 1, epiId: 2, lotCode: 'A', manufacturerLot: 'L-2026-A', caNumber: '67890',
          caCheckedAt: '2026-03-10', pieceValidUntil: '2026-12-15', qtyOnHand: 117, size: 'Único',
        },
        {
          id: 2, epiId: 2, lotCode: 'B', manufacturerLot: 'L-2025-B', caNumber: '67890',
          caCheckedAt: '2026-03-10', pieceValidUntil: '2026-08-01', qtyOnHand: 0, size: 'Único',
        },
        {
          id: 3, epiId: 1, lotCode: 'C', manufacturerLot: 'L-OC-01', caNumber: '12345',
          caCheckedAt: '2026-02-01', pieceValidUntil: '2026-10-20', qtyOnHand: 45, size: 'Único',
        },
      ],
      matrix: [
        { id: 1, jobRoleId: 1, epiId: 1, expectedCa: '12345', assignment: 'INDIVIDUAL', trainingRequired: false },
        { id: 2, jobRoleId: 1, epiId: 2, expectedCa: '67890', assignment: 'INDIVIDUAL', trainingRequired: true },
      ],
      periodicity: [
        { epiId: 1, days: 365, warnDays: 30 },
        { epiId: 2, days: 180, warnDays: 15 },
      ],
      issuances: [
        {
          id: 1, code: 'ENT-2026-8840', employeeId: 1, epiId: 1, lotId: 3, caNumber: '12345', qty: 1,
          issuedAt: '2026-08-12T09:30:00', status: 'ACTIVE', awareness: true, termAccepted: true,
          matrixException: false, operatorLogin: 'almox', requestId: null,
        },
        {
          id: 2, code: 'ENT-2026-8839', employeeId: 3, epiId: 2, lotId: 1, caNumber: '67890', qty: 1,
          issuedAt: '2026-07-01T10:00:00', status: 'ACTIVE', awareness: true, termAccepted: true,
          matrixException: false, operatorLogin: 'almox', requestId: null,
        },
      ],
      returns: [],
      reversals: [],
      requests: [
        {
          id: 1042, employeeId: 1, epiId: 2, qty: 1, reason: 'Reposição desgaste', status: 'APPROVED',
          requestedAt: '2026-09-20T14:00:00', requestedBy: 'gestor', notes: '',
        },
      ],
      auditLog: [
        { id: 1, at: '2026-09-28T03:12:44', user: 'system', action: 'CAEPI_IMPORT_OK', detail: 'Carga CAEPI-20260928-928 · 12418 registros' },
        { id: 2, at: '2026-08-12T09:30:05', user: 'almox', action: 'ISSUANCE_CREATE', detail: 'ENT-2026-8840 · João da Silva · Óculos incolor' },
      ],
      params: {
        unitName: 'Itupeva',
        legalName: 'Indústria Exemplo S.A.',
        cnpj: '12.345.678/0001-90',
        deploymentMode: 'local',
        caepiImportTime: '03:00',
        units: ['Itupeva', 'Campinas'],
      },
      users: [
        { id: 1, login: 'admin', displayName: 'Administrador', papel: 'Admin', active: true, blocked: false, mustChangePassword: false },
        { id: 2, login: 'maria', displayName: 'Maria Silva', papel: 'Almoxarife', active: true, blocked: false, mustChangePassword: false },
        { id: 3, login: 'novo', displayName: 'Operador Novo', papel: 'Almoxarife', active: true, blocked: false, mustChangePassword: true },
        { id: 4, login: 'sesmt', displayName: 'Equipe SESMT', papel: 'SESMT', active: true, blocked: false, mustChangePassword: false },
        { id: 5, login: 'gestor', displayName: 'Carlos Gestor', papel: 'Gestor', active: true, blocked: false, mustChangePassword: false },
        { id: 6, login: 'consulta', displayName: 'Perfil Consulta', papel: 'Consulta', active: true, blocked: false, mustChangePassword: false },
      ],
    };
  }

  function seed() {
    const op = seedOperational();
    return {
      departments: [
        { id: 1, name: 'Produção', active: true },
        { id: 2, name: 'Manutenção', active: true },
        { id: 3, name: 'SESMT', active: true },
      ],
      jobRoles: [
        { id: 1, name: 'Operador de máquina', departmentId: 1, active: true, hasActiveEmployees: true },
        { id: 2, name: 'Almoxarife', departmentId: 1, active: true, hasActiveEmployees: false },
        { id: 3, name: 'Auxiliar', departmentId: 2, active: true, hasActiveEmployees: false },
      ],
      employees: [
        { id: 1, employeeCode: '000123', fullName: 'João da Silva', departmentId: 1, jobRoleId: 1, active: true },
        { id: 2, employeeCode: '000124', fullName: 'Ana Costa', departmentId: 2, jobRoleId: 3, active: true },
        { id: 3, employeeCode: '000099', fullName: 'Pedro Souza', departmentId: 1, jobRoleId: 1, active: false },
      ],
      epis: [
        { id: 1, code: 'EPI-001', description: 'Óculos incolor', annexGroup: 'A', manufacturer: 'Fabricante A', active: true },
        { id: 2, code: 'EPI-002', description: 'Protetor auricular plug', annexGroup: 'F', manufacturer: '3M Brasil', active: true },
        { id: 3, code: 'EPI-003', description: 'Luva nitrílica especial', annexGroup: 'D', manufacturer: 'Fabricante C', active: false },
      ],
      caBindings: [
        {
          id: 1, epiId: 1, caNumber: '12345', caStatus: 'ACTIVE', validFrom: '', validUntil: '',
          officialCheckAt: '2026-09-28T03:12',
          officialNote: 'Consulta CAEPI carga CAEPI-20260928-928. CA 12345 — ATIVO.',
          attachmentName: null, active: true,
        },
        {
          id: 2, epiId: 2, caNumber: '67890', caStatus: 'ACTIVE', validFrom: '', validUntil: '',
          officialCheckAt: '2026-09-28T03:12',
          officialNote: 'Consulta base CAEPI carga CAEPI-20260928-928 (28/09/2026 03:12). CA 67890 — situação ATIVO.',
          attachmentName: null, active: true,
        },
        {
          id: 3, epiId: 2, caNumber: '45001', caStatus: 'EXPIRED', validFrom: '2020-01-01', validUntil: '2026-06-01',
          officialCheckAt: '2026-01-10T10:00', officialNote: 'Histórico — CA expirado.', attachmentName: null, active: false,
        },
      ],
      ...op,
      nextId: {
        department: 4, jobRole: 4, employee: 4, epi: 4, caBinding: 4,
        lot: 4, matrix: 3, issuance: 3, return: 1, reversal: 1, request: 1043, audit: 3, user: 7,
      },
    };
  }

  function ensureSchema(db) {
    const fresh = seed();
    if (!db.caepi) Object.assign(db, seedOperational());
    if (!db.nextId.lot) db.nextId.lot = 4;
    if (!db.nextId.matrix) db.nextId.matrix = 3;
    if (!db.nextId.issuance) db.nextId.issuance = 2;
    if (!db.nextId.audit) db.nextId.audit = 3;
    if (!db.caBindings.some(b => b.epiId === 1 && b.active)) {
      db.caBindings.push(fresh.caBindings[0]);
    }
    (db.periodicity || []).forEach(p => {
      if (p.warnDays == null) p.warnDays = p.days >= 365 ? 30 : 15;
    });
    if (!db.params.legalName) {
      Object.assign(db.params, seed().params);
    }
    if (!db.params.units) db.params.units = [db.params.unitName || 'Itupeva'];
    (db.users || []).forEach((u, i) => {
      if (u.id == null) u.id = i + 1;
      if (u.displayName == null) u.displayName = u.login;
      if (u.mustChangePassword == null) u.mustChangePassword = false;
    });
    if (!db.nextId.user) db.nextId.user = (db.users.length || 0) + 1;
    if (!db.caepi.index) db.caepi.index = {};
    if (db.caepi.indexInIdb == null) db.caepi.indexInIdb = false;
    if (db.caepi.lastMode == null) db.caepi.lastMode = 'AUTO';
    if (db.caepi.autoEnabled == null) db.caepi.autoEnabled = true;
    return db;
  }

  function hostWindow() {
    try {
      if (global.parent && global.parent !== global && global.parent.__enr6Db) return global.parent;
    } catch (e) { /* cross-origin */ }
    return global;
  }

  function readLegacySessionSnapshot() {
    const host = hostWindow();
    try {
      let raw = host.sessionStorage.getItem(LEGACY_STORAGE_KEY);
      if (!raw) raw = host.localStorage.getItem(LEGACY_STORAGE_KEY);
      if (raw) return JSON.parse(raw);
    } catch (e) { /* ignore */ }
    return null;
  }

  function clearLegacySessionSnapshot() {
    const host = hostWindow();
    try { host.sessionStorage.removeItem(LEGACY_STORAGE_KEY); } catch (e) { /* ignore */ }
    try { host.localStorage.removeItem(LEGACY_STORAGE_KEY); } catch (e) { /* ignore */ }
  }

  function idb() {
    return global.Enr6MockIdb;
  }

  function syncCaepiMemory(host, db) {
    if (host.__enr6CaepiIndex && Object.keys(host.__enr6CaepiIndex).length) return;
    if (db.caepi && db.caepi.index && Object.keys(db.caepi.index).length) {
      host.__enr6CaepiIndex = db.caepi.index;
    }
  }

  function dbForJson(db) {
    const copy = JSON.parse(JSON.stringify(db));
    if (copy.caepi && copy.caepi.indexInIdb && copy.caepi.index) {
      delete copy.caepi.index;
    }
    return copy;
  }

  function writeMemory(db) {
    const host = hostWindow();
    host.__enr6Db = db;
    syncCaepiMemory(host, db);
  }

  function persistToIdb(db) {
    const store = idb();
    if (!store) return Promise.reject(new Error('mock-idb.js não carregado.'));
    writeMemory(db);
    const host = hostWindow();
    const payload = dbForJson(db);
    let chain = store.saveApp(payload);
    if (db.caepi.indexInIdb && host.__enr6CaepiIndex) {
      chain = chain.then(function () {
        return store.replaceCaIndex(host.__enr6CaepiIndex);
      });
    }
    return chain;
  }

  function getCaepiIndexMap() {
    const host = hostWindow();
    if (host.__enr6CaepiIndex && Object.keys(host.__enr6CaepiIndex).length) {
      return host.__enr6CaepiIndex;
    }
    const db = loadDb();
    return db.caepi.index || {};
  }

  function shouldUseCaepiIdb(index) {
    return Object.keys(index || {}).length > CAEPI_IDB_THRESHOLD;
  }

  function applyCaepiIndex(db, index) {
    const host = hostWindow();
    host.__enr6CaepiIndex = index;
    db.caepi.index = index;
    if (shouldUseCaepiIdb(index)) {
      db.caepi.indexInIdb = true;
    } else {
      db.caepi.indexInIdb = false;
    }
  }

  function loadDb() {
    const host = hostWindow();
    if (host.__enr6Db) return ensureSchema(host.__enr6Db);
    host.__enr6Db = seed();
    return ensureSchema(host.__enr6Db);
  }

  function persist(db) {
    writeMemory(db);
    persistToIdb(db).catch(function (err) {
      console.error('Enr6MockStore persist', err);
    });
  }

  function bootstrap(onProgress) {
    const host = hostWindow();
    if (host.__enr6StorageReady && host.__enr6Db) {
      return Promise.resolve(loadDb());
    }
    if (host.__enr6BootstrapPromise) return host.__enr6BootstrapPromise;

    host.__enr6BootstrapPromise = (function () {
      const store = idb();
      if (!store) return Promise.reject(new Error('mock-idb.js não carregado.'));

      function finishHydrate(db) {
        host.__enr6Db = ensureSchema(db);
        host.__enr6StorageReady = true;
        syncCaepiMemory(host, host.__enr6Db);
        return loadDb();
      }

      function hydrateCaFromIdb(db) {
        if (!db.caepi.indexInIdb) {
          host.__enr6CaepiIndex = db.caepi.index || {};
          host.__enr6CaepiHydrated = true;
          return Promise.resolve(finishHydrate(db));
        }
        return store.loadCaIndex(onProgress).then(function (index) {
          host.__enr6CaepiIndex = index;
          db.caepi.index = index;
          host.__enr6CaepiHydrated = true;
          return finishHydrate(db);
        });
      }

      return store.loadApp().then(function (app) {
        if (app) return hydrateCaFromIdb(app);
        const legacy = readLegacySessionSnapshot();
        if (legacy) {
          clearLegacySessionSnapshot();
          return hydrateCaFromIdb(legacy).then(function (db) {
            return persistToIdb(db).then(function () { return db; });
          });
        }
        return store.migrateLegacyCaepiDbIfNeeded().then(function (legacyCa) {
          const db = seed();
          if (legacyCa && Object.keys(legacyCa).length) {
            applyCaepiIndex(db, legacyCa);
            db.caepi.valid = true;
            db.caepi.loadedAt = db.caepi.loadedAt || new Date().toISOString();
            db.caepi.recordCount = Object.keys(legacyCa).length;
            db.caepi.lastMode = 'MANUAL';
            db.caepi.sourceFormat = 'CAEPI_RELATORIO_CSV';
          }
          return persistToIdb(db).then(function () {
            return hydrateCaFromIdb(db);
          });
        });
      });
    })();

    return host.__enr6BootstrapPromise;
  }

  function norm(s) { return (s || '').trim().toLowerCase(); }

  function syncJobRoleFlags(db) {
    db.jobRoles.forEach(j => {
      j.hasActiveEmployees = db.employees.some(e => e.jobRoleId === j.id && e.active);
    });
  }

  function lotExpired(lot) {
    if (!lot || !lot.pieceValidUntil) return false;
    return lot.pieceValidUntil < todayStr();
  }

  function activeCaNumber(db, epiId) {
    const b = db.caBindings.find(x => x.epiId === epiId && x.active && x.caStatus === 'ACTIVE');
    return b ? b.caNumber : null;
  }

  function epiById(db, id) {
    return db.epis.find(e => e.id === id);
  }

  function resolveIssuance(row) {
    const db = loadDb();
    const emp = db.employees.find(e => e.id === row.employeeId);
    const ep = epiById(db, row.epiId);
    const lot = db.lots.find(l => l.id === row.lotId);
    const er = emp ? api.resolveEmployeeRow(emp) : null;
    return {
      ...row,
      employeeName: emp ? emp.fullName : '—',
      employeeCode: emp ? emp.employeeCode : '—',
      epiName: ep ? ep.description : '—',
      epiCode: ep ? ep.code : '—',
      lotCode: lot ? lot.lotCode : '—',
      departmentName: er ? er.departmentName : '—',
      jobRoleName: er ? er.jobRoleName : '—',
    };
  }

  const api = {
    bootstrap(onProgress) { return bootstrap(onProgress); },
    initHost(onProgress) { return bootstrap(onProgress); },
    initClient(onProgress) {
      const host = hostWindow();
      if (host.__enr6StorageReady && host.__enr6Db) {
        if (host.__enr6Db.caepi.indexInIdb && !host.__enr6CaepiHydrated) {
          return api.hydrateCaepiFromIdb(onProgress);
        }
        return Promise.resolve(loadDb());
      }
      if (host !== global && host.__enr6Db && host.__enr6StorageReady) {
        return Promise.resolve(loadDb());
      }
      return bootstrap(onProgress);
    },
    isCaepiIndexReady() {
      const host = hostWindow();
      if (host.__enr6CaepiHydrated) return true;
      const db = loadDb();
      if (!db.caepi.indexInIdb) return true;
      return !!(host.__enr6CaepiIndex && Object.keys(host.__enr6CaepiIndex).length);
    },
    hydrateCaepiFromIdb(onProgress) {
      const host = hostWindow();
      const db = loadDb();
      if (host.__enr6CaepiHydrated && host.__enr6CaepiIndex) {
        return Promise.resolve(host.__enr6CaepiIndex);
      }
      if (!db.caepi.indexInIdb) {
        host.__enr6CaepiIndex = db.caepi.index || {};
        host.__enr6CaepiHydrated = true;
        return Promise.resolve(host.__enr6CaepiIndex);
      }
      const store = idb();
      if (!store) {
        return Promise.reject(new Error('mock-idb.js não carregado.'));
      }
      return store.loadCaIndex(onProgress).then(function (index) {
        host.__enr6CaepiIndex = index;
        db.caepi.index = index;
        host.__enr6CaepiHydrated = true;
        host.__enr6Db = db;
        return index;
      });
    },
    reset() {
      const host = hostWindow();
      host.__enr6CaepiIndex = null;
      host.__enr6CaepiHydrated = false;
      host.__enr6StorageReady = false;
      host.__enr6BootstrapPromise = null;
      clearLegacySessionSnapshot();
      const store = idb();
      const done = function () {
        const db = seed();
        host.__enr6CaepiIndex = db.caepi.index;
        host.__enr6CaepiHydrated = true;
        return persistToIdb(db).then(function () {
          host.__enr6StorageReady = true;
          host.__enr6Db = db;
          return db;
        });
      };
      if (store && store.clearAll) return store.clearAll().then(done);
      return done();
    },
    getSessionUser() {
      try {
        const w = global.parent && global.parent !== global ? global.parent : global;
        const raw = w.sessionStorage.getItem('enr6_mock_user');
        if (raw) return JSON.parse(raw);
      } catch (e) { /* ignore */ }
      return { login: 'admin', papel: 'Admin', nome: 'Admin' };
    },
    isCaepiValid() {
      return !!loadDb().caepi.valid;
    },
    getCaepi() {
      const c = loadDb().caepi;
      const out = { ...c };
      if (c.indexInIdb) delete out.index;
      else out.index = c.index || {};
      out.indexReady = api.isCaepiIndexReady();
      return out;
    },
    setCaepiValid(valid) {
      const db = loadDb();
      db.caepi.valid = !!valid;
      persist(db);
      api.audit(valid ? 'CAEPI_VALID' : 'CAEPI_DEGRADED', valid ? 'Carga confirmada' : 'Mutações EPI/CA bloqueadas');
      try {
        if (global.parent && global.parent !== global) {
          global.parent.postMessage({ type: 'enr6-caepi-degraded', value: !valid }, '*');
        }
      } catch (e) { /* ignore */ }
      return db.caepi;
    },
    lookupCa(caNumber) {
      const ca = String(caNumber || '').replace(/\D/g, '');
      const idx = getCaepiIndexMap();
      return idx[ca] || null;
    },
    lookupCaAsync(caNumber) {
      const ca = String(caNumber || '').replace(/\D/g, '');
      const hit = api.lookupCa(ca);
      if (hit) return Promise.resolve(hit);
      const db = loadDb();
      const store = idb();
      if (!db.caepi.indexInIdb || !store) return Promise.resolve(null);
      return store.getOneCa(ca);
    },
    publishCaepiImport(result) {
      const db = loadDb();
      const now = new Date().toISOString();
      const mode = result.mode || 'MANUAL';
      if (result.ok) {
        db.caepi.valid = true;
        db.caepi.loadedAt = now;
        db.caepi.batchId = result.batchId || ('CAEPI-' + now.slice(0, 10).replace(/-/g, '') + '-' + mode);
        db.caepi.recordCount = result.recordCount || 0;
        db.caepi.lastMode = mode;
        if (result.sourceFormat) db.caepi.sourceFormat = result.sourceFormat;
        if (result.variantRows != null) db.caepi.variantRows = result.variantRows;
        if (result.index) applyCaepiIndex(db, result.index);
        db.caepi.lastAutoError = null;
        if (db.caepi.indexInIdb) db.caepi.idbSavedAt = db.caepi.idbSavedAt || null;
        db.caepi.history.unshift({
          at: now, ok: true, durationMin: result.durationMin || null, mode,
        });
        const fmt = db.caepi.sourceFormat ? ' · ' + db.caepi.sourceFormat : '';
        api.audit('CAEPI_IMPORT_OK', mode + fmt + ' · ' + db.caepi.batchId + ' · ' + db.caepi.recordCount + ' CAs');
      } else {
        if (mode === 'AUTO') {
          db.caepi.lastAutoAttemptAt = now;
          db.caepi.lastAutoError = result.error || 'Falha';
          db.caepi.valid = db.caepi.recordCount > 0 && !!db.caepi.loadedAt;
        }
        db.caepi.history.unshift({
          at: now, ok: false, error: result.error, mode,
        });
        api.audit('CAEPI_IMPORT_FAIL', mode + ' · ' + (result.error || 'erro'));
      }
      persist(db);
      try {
        if (global.parent && global.parent !== global) {
          global.parent.postMessage({ type: 'enr6-caepi-degraded', value: !db.caepi.valid }, '*');
        }
      } catch (e) { /* ignore */ }
      return db.caepi;
    },
    importCaepiFromText(text, mode) {
      return api.importCaepiFromTextAsync(text, mode || 'MANUAL');
    },
    importCaepiFromTextAsync(text, mode, onProgress) {
      const parser = global.Enr6CaepiParser;
      if (!parser) return Promise.reject(new Error('Parser CAEPI não carregado.'));
      let parsed;
      try {
        parsed = parser.parseText(text, { onProgress: onProgress });
      } catch (err) {
        return Promise.reject(err);
      }
      if (parsed.recordCount < 1) {
        return Promise.reject(new Error('CAE-003 Nenhum CA reconhecido no arquivo (TGG pipe ou RelatorioCA CSV).'));
      }
      api.publishCaepiImport({
        ok: true,
        mode: mode || 'MANUAL',
        index: parsed.index,
        recordCount: parsed.recordCount,
        sourceFormat: parsed.format,
        variantRows: parsed.variantRows,
        batchId: 'CAEPI-MANUAL-' + Date.now(),
        durationMin: null,
      });
      const db = loadDb();
      const store = idb();
      if (!db.caepi.indexInIdb || !store) {
        return Promise.resolve(db.caepi);
      }
      const now = new Date().toISOString();
      return store.replaceCaIndex(parsed.index, onProgress).then(function () {
        return persistToIdb(loadDb()).then(function () {
          const d = loadDb();
          d.caepi.idbSavedAt = now;
          writeMemory(d);
          hostWindow().__enr6CaepiHydrated = true;
          return d.caepi;
        });
      });
    },
    runCaepiImportAutoSimulated() {
      return api.publishCaepiImport({
        ok: true,
        mode: 'AUTO',
        recordCount: loadDb().caepi.recordCount || 12418,
        batchId: 'CAEPI-AUTO-SIM-' + todayStr().replace(/-/g, ''),
        durationMin: 12,
        index: getCaepiIndexMap(),
      });
    },
    runCaepiImportAutoSimulatedFail(error) {
      return api.publishCaepiImport({
        ok: false,
        mode: 'AUTO',
        error: error || 'CAE-001 Fonte indisponível (simulação)',
      });
    },
    /** @deprecated use runCaepiImportAutoSimulated */
    runCaepiImportMock() {
      return api.runCaepiImportAutoSimulated();
    },
    audit(action, detail, ctx) {
      const db = loadDb();
      const user = api.getSessionUser();
      const row = {
        id: db.nextId.audit++,
        at: new Date().toISOString(),
        user: (ctx && ctx.user) || user.login || 'mock',
        action,
        detail: detail || '',
      };
      db.auditLog.unshift(row);
      persist(db);
      return row;
    },
    getAuditLog(limit) {
      const n = limit || 200;
      return loadDb().auditLog.slice(0, n);
    },
    getParams() {
      const p = loadDb().params;
      return { ...p, units: (p.units || [p.unitName]).slice() };
    },
    setParams(patch) {
      const db = loadDb();
      Object.assign(db.params, patch);
      if (patch.unitName && db.params.units && !db.params.units.includes(patch.unitName)) {
        db.params.units.push(patch.unitName);
      }
      api.audit('PARAMS_UPDATE', 'Unidade ' + db.params.unitName + ' · ' + (db.params.legalName || ''));
      persist(db);
      return db.params;
    },
    getUsers() {
      return loadDb().users.slice().sort((a, b) => norm(a.login).localeCompare(norm(b.login)));
    },
    getUserById(id) {
      return loadDb().users.find(u => u.id === id) || null;
    },
    createUser(payload) {
      const db = loadDb();
      const login = (payload.login || '').trim().toLowerCase();
      const displayName = (payload.displayName || '').trim();
      const papel = payload.papel || 'Consulta';
      if (!login) throw new Error('Informe o login.');
      if (db.users.some(u => norm(u.login) === login)) throw new Error('Login já existe.');
      const row = {
        id: db.nextId.user++,
        login,
        displayName: displayName || login,
        papel,
        active: true,
        blocked: false,
        mustChangePassword: true,
      };
      db.users.push(row);
      api.audit('USER_CREATE', login + ' · ' + papel);
      persist(db);
      return row;
    },
    updateUser(id, patch) {
      const db = loadDb();
      const u = db.users.find(x => x.id === id);
      if (!u) throw new Error('Usuário não encontrado.');
      if (u.login === 'admin' && patch.active === false) throw new Error('Não é possível inativar o admin principal.');
      if (patch.displayName) u.displayName = patch.displayName.trim();
      if (patch.papel) u.papel = patch.papel;
      if (typeof patch.active === 'boolean') u.active = patch.active;
      if (typeof patch.blocked === 'boolean') u.blocked = patch.blocked;
      api.audit('USER_UPDATE', u.login);
      persist(db);
      return u;
    },
    resetUserCredential(id) {
      const db = loadDb();
      const u = db.users.find(x => x.id === id);
      if (!u) throw new Error('Usuário não encontrado.');
      u.mustChangePassword = true;
      u.blocked = false;
      api.audit('USER_PASSWORD_RESET', u.login);
      persist(db);
      return u;
    },
    countUsersNeedingAttention() {
      return loadDb().users.filter(u => u.mustChangePassword || u.blocked || !u.active).length;
    },
    runReport(type, opts) {
      const db = loadDb();
      opts = opts || {};
      const from = (opts.dateFrom || '2020-01-01').slice(0, 10);
      const to = (opts.dateTo || '2099-12-31').slice(0, 10);
      const empQ = norm(opts.employeeQ);
      const inPeriod = iso => {
        const d = (iso || '').slice(0, 10);
        return d >= from && d <= to;
      };

      if (type === 'ficha') {
        let emp = null;
        if (empQ) {
          emp = db.employees.find(e => e.employeeCode.includes(opts.employeeQ) || norm(e.fullName).includes(empQ));
        }
        const rows = db.issuances.filter(i => {
          if (emp && i.employeeId !== emp.id) return false;
          if (!inPeriod(i.issuedAt)) return false;
          return true;
        }).map(i => resolveIssuance(i));
        return {
          title: 'Ficha por trabalhador',
          count: rows.length,
          lines: rows.slice(0, 50).map(r =>
            r.employeeName + ' · ' + r.issuedAt.slice(0, 10) + ' · ' + r.epiName + ' · CA ' + r.caNumber + ' · ' + r.code
          ),
          link: emp ? '05-historico-trabalhador.html' : '05-historico-trabalhador.html',
        };
      }
      if (type === 'hist') {
        const rows = db.issuances.filter(i => inPeriod(i.issuedAt)).map(resolveIssuance);
        return {
          title: 'Histórico por EPI / CA / lote',
          count: rows.length,
          lines: rows.slice(0, 50).map(r =>
            r.epiName + ' · CA ' + r.caNumber + ' · Lote ' + r.lotCode + ' · ' + r.employeeName + ' · ' + r.code
          ),
          link: '05-historico-trabalhador.html',
        };
      }
      if (type === 'cob') {
        const rep = api.getCoverageReport({ status: 'all' });
        return {
          title: 'Cobertura matriz × vigente',
          count: rep.length,
          lines: rep.slice(0, 50).map(r =>
            r.employeeName + ' · exigidos ' + r.required + ' · vigentes ' + r.vigente + ' · ' + r.gapLabel
          ),
          link: '14-cobertura.html',
        };
      }
      if (type === 'pend') {
        const pend = api.getPendencies();
        return {
          title: 'Pendências operacionais',
          count: pend.length,
          lines: pend.slice(0, 50).map(p => p.type + ' · ' + p.employeeName + ' · ' + p.detail),
          link: '15-pendencias.html',
        };
      }
      return { title: 'Relatório', count: 0, lines: [], link: '13-relatorios-hub.html' };
    },

    /* --- cadastros (existentes) --- */
    getDepartments(filter) {
      const db = loadDb();
      const q = norm(filter);
      return db.departments.filter(d => !q || norm(d.name).includes(q));
    },
    createDepartment(name) {
      const db = loadDb();
      name = (name || '').trim();
      if (!name) throw new Error('CAD-020 Informe o nome do setor.');
      if (db.departments.some(d => norm(d.name) === norm(name))) throw new Error('CAD-021 Já existe setor com este nome.');
      const row = { id: db.nextId.department++, name, active: true };
      db.departments.push(row);
      api.audit('DEPARTMENT_CREATE', name);
      persist(db);
      return row;
    },
    updateDepartment(id, name) {
      const db = loadDb();
      const d = db.departments.find(x => x.id === id);
      if (!d) throw new Error('CAD-023 Setor não encontrado.');
      name = (name || '').trim();
      if (!name) throw new Error('CAD-020 Informe o nome do setor.');
      if (db.departments.some(x => x.id !== id && norm(x.name) === norm(name))) throw new Error('CAD-021 Já existe setor com este nome.');
      d.name = name;
      persist(db);
      return d;
    },
    setDepartmentStatus(id, active) {
      const db = loadDb();
      const d = db.departments.find(x => x.id === id);
      if (!d) throw new Error('CAD-023 Setor não encontrado.');
      if (!active) {
        const activeRoles = db.jobRoles.filter(j => j.departmentId === id && j.active).length;
        if (activeRoles > 0) throw new Error('CAD-024 Não é possível inativar: existem funções ativas neste setor.');
      }
      d.active = active;
      persist(db);
      return d;
    },
    countActiveRolesInDepartment(departmentId) {
      return loadDb().jobRoles.filter(j => j.departmentId === departmentId && j.active).length;
    },
    getJobRoles(filter, departmentId) {
      const db = loadDb();
      const q = norm(filter);
      return db.jobRoles.filter(j => {
        if (departmentId && j.departmentId !== departmentId) return false;
        if (q && !norm(j.name).includes(q)) return false;
        return true;
      });
    },
    createJobRole(name, departmentId) {
      const db = loadDb();
      name = (name || '').trim();
      if (!departmentId) throw new Error('CAD-026 Selecione um setor ativo.');
      const dep = db.departments.find(d => d.id === departmentId);
      if (!dep || !dep.active) throw new Error('CAD-026 Setor inválido ou inativo.');
      if (!name) throw new Error('Informe o nome da função.');
      if (db.jobRoles.some(j => j.departmentId === departmentId && norm(j.name) === norm(name))) {
        throw new Error('CAD-025 Já existe função com este nome neste setor.');
      }
      const row = { id: db.nextId.jobRole++, name, departmentId, active: true, hasActiveEmployees: false };
      db.jobRoles.push(row);
      persist(db);
      return row;
    },
    updateJobRole(id, name, departmentId) {
      const db = loadDb();
      const j = db.jobRoles.find(x => x.id === id);
      if (!j) throw new Error('Função não encontrada.');
      const dep = db.departments.find(d => d.id === departmentId);
      if (!dep || !dep.active) throw new Error('CAD-026 Setor inválido ou inativo.');
      name = (name || '').trim();
      if (!name) throw new Error('Informe o nome da função.');
      if (db.jobRoles.some(x => x.id !== id && x.departmentId === departmentId && norm(x.name) === norm(name))) {
        throw new Error('CAD-025 Já existe função com este nome neste setor.');
      }
      j.name = name;
      j.departmentId = departmentId;
      persist(db);
      return j;
    },
    setJobRoleStatus(id, active) {
      const db = loadDb();
      const j = db.jobRoles.find(x => x.id === id);
      if (!j) throw new Error('Função não encontrada.');
      if (!active && j.hasActiveEmployees) throw new Error('CAD-028 Não é possível inativar: empregados ativos vinculados.');
      if (active) {
        const dep = db.departments.find(d => d.id === j.departmentId);
        if (!dep || !dep.active) throw new Error('CAD-026 Reative o setor antes da função.');
      }
      j.active = active;
      persist(db);
      return j;
    },
    getEmployees(filter) {
      const db = loadDb();
      const q = norm(filter);
      return db.employees.filter(e => {
        if (!q) return true;
        return norm(e.fullName).includes(q) || e.employeeCode.includes(q);
      });
    },
    findActiveEmployee(query) {
      const q = norm(query);
      if (!q) return null;
      const db = loadDb();
      return db.employees.find(e =>
        e.active && (e.employeeCode.includes(q) || norm(e.fullName).includes(q))
      ) || null;
    },
    resolveEmployeeRow(e) {
      const db = loadDb();
      const dep = db.departments.find(d => d.id === e.departmentId);
      const job = db.jobRoles.find(j => j.id === e.jobRoleId);
      return {
        ...e,
        departmentName: dep ? dep.name : '—',
        jobRoleName: job ? job.name : '—',
      };
    },
    createEmployee(payload) {
      const db = loadDb();
      const code = (payload.employeeCode || '').trim();
      const fullName = (payload.fullName || '').trim();
      if (!code || !fullName) throw new Error('Matrícula e nome são obrigatórios.');
      if (db.employees.some(e => e.employeeCode === code)) throw new Error('Matrícula já cadastrada.');
      const row = {
        id: db.nextId.employee++,
        employeeCode: code,
        fullName,
        departmentId: payload.departmentId,
        jobRoleId: payload.jobRoleId,
        active: payload.active !== false,
      };
      db.employees.push(row);
      syncJobRoleFlags(db);
      persist(db);
      api.audit('EMPLOYEE_CREATE', row.employeeCode + ' · ' + row.fullName);
      return row;
    },
    updateEmployee(id, payload) {
      const db = loadDb();
      const e = db.employees.find(x => x.id === id);
      if (!e) throw new Error('Empregado não encontrado.');
      if (payload.employeeCode != null) {
        const code = String(payload.employeeCode).trim();
        if (!code) throw new Error('Matrícula é obrigatória.');
        if (db.employees.some(x => x.id !== id && x.employeeCode === code)) {
          throw new Error('Matrícula já cadastrada.');
        }
        e.employeeCode = code;
      }
      if (payload.fullName != null) {
        const fullName = String(payload.fullName).trim();
        if (!fullName) throw new Error('Nome é obrigatório.');
        e.fullName = fullName;
      }
      if (payload.departmentId != null) e.departmentId = payload.departmentId;
      if (payload.jobRoleId != null) e.jobRoleId = payload.jobRoleId;
      if (typeof payload.active === 'boolean') e.active = payload.active;
      syncJobRoleFlags(db);
      persist(db);
      api.audit('EMPLOYEE_UPDATE', e.employeeCode + ' · ' + e.fullName);
      return e;
    },
    getEpis(filter) {
      const db = loadDb();
      const q = norm(filter);
      return db.epis.filter(ep => !q || norm(ep.description).includes(q) || norm(ep.code).includes(q));
    },
    epiHasActiveCa(epiId) {
      return loadDb().caBindings.some(b => b.epiId === epiId && b.active && b.caStatus === 'ACTIVE');
    },
    createEpi(payload) {
      if (!api.isCaepiValid()) throw new Error('CAEPI inválida — mutações EPI bloqueadas.');
      const db = loadDb();
      const description = (payload.description || '').trim();
      if (!description) throw new Error('CAD-031 Descrição obrigatória.');
      const id = db.nextId.epi++;
      const row = {
        id,
        code: (payload.code || '').trim() || ('EPI-' + String(id).padStart(3, '0')),
        description,
        annexGroup: payload.annexGroup || 'A',
        manufacturer: (payload.manufacturer || '').trim() || '—',
        active: false,
      };
      db.epis.push(row);
      persist(db);
      return row;
    },
    updateEpi(id, payload) {
      if (!api.isCaepiValid() && (payload.active !== undefined || payload.description)) {
        throw new Error('CAEPI inválida — mutações EPI bloqueadas.');
      }
      const db = loadDb();
      const ep = db.epis.find(x => x.id === id);
      if (!ep) throw new Error('EPI não encontrado.');
      if (payload.description) ep.description = payload.description.trim();
      if (payload.annexGroup) ep.annexGroup = payload.annexGroup;
      if (payload.manufacturer) ep.manufacturer = payload.manufacturer.trim();
      if (typeof payload.active === 'boolean') {
        if (payload.active && !api.epiHasActiveCa(id)) throw new Error('EPI sem CA ativo — ativação operacional bloqueada.');
        ep.active = payload.active;
      }
      persist(db);
      return ep;
    },
    getCaBindings(epiId) {
      return loadDb().caBindings.filter(b => b.epiId === epiId);
    },
    createCaBinding(payload) {
      if (!api.isCaepiValid()) throw new Error('CAEPI inválida — mutações CA bloqueadas.');
      const db = loadDb();
      const row = { id: db.nextId.caBinding++, ...payload };
      db.caBindings.push(row);
      persist(db);
      return row;
    },
    updateCaBinding(id, payload) {
      if (!api.isCaepiValid()) throw new Error('CAEPI inválida — mutações CA bloqueadas.');
      const db = loadDb();
      const b = db.caBindings.find(x => x.id === id);
      if (!b) throw new Error('Vínculo não encontrado.');
      Object.assign(b, payload);
      persist(db);
      return b;
    },
    departmentName(id) {
      const d = loadDb().departments.find(x => x.id === id);
      return d ? d.name : '—';
    },

    /* --- lotes --- */
    getLots(filter) {
      const db = loadDb();
      const q = norm(filter);
      return db.lots.filter(l => {
        if (!q) return true;
        const ep = epiById(db, l.epiId);
        const label = (ep ? ep.description + ' ' + l.lotCode : l.lotCode);
        return norm(label).includes(q);
      }).map(l => {
        const ep = epiById(db, l.epiId);
        return {
          ...l,
          epiName: ep ? ep.description : '—',
          expired: lotExpired(l),
        };
      });
    },
    createLot(payload) {
      const db = loadDb();
      const epiId = payload.epiId;
      const ep = epiById(db, epiId);
      if (!ep || !ep.active) throw new Error('Selecione EPI ativo.');
      const qty = parseInt(payload.qtyOnHand, 10);
      if (!qty || qty < 1) throw new Error('Quantidade inválida.');
      const row = {
        id: db.nextId.lot++,
        epiId,
        lotCode: (payload.lotCode || '').trim() || ('L' + db.nextId.lot),
        manufacturerLot: (payload.manufacturerLot || '').trim(),
        caNumber: (payload.caNumber || '').trim(),
        caCheckedAt: payload.caCheckedAt || todayStr(),
        pieceValidUntil: payload.pieceValidUntil,
        qtyOnHand: qty,
        size: (payload.size || '').trim() || 'Único',
      };
      if (!row.pieceValidUntil) throw new Error('Informe validade da peça.');
      db.lots.push(row);
      api.audit('LOT_RECEIVE', row.lotCode + ' · ' + ep.description + ' · ' + qty + ' un');
      persist(db);
      return row;
    },

    /* --- matriz & periodicidade --- */
    getMatrixForJobRole(jobRoleId) {
      const db = loadDb();
      return db.matrix.filter(m => m.jobRoleId === jobRoleId).map(m => {
        const ep = epiById(db, m.epiId);
        return { ...m, epiName: ep ? ep.description : '—', annexGroup: ep ? ep.annexGroup : '—' };
      });
    },
    addMatrixRow(jobRoleId, epiId) {
      const db = loadDb();
      if (db.matrix.some(m => m.jobRoleId === jobRoleId && m.epiId === epiId)) {
        throw new Error('EPI já na matriz desta função.');
      }
      const row = {
        id: db.nextId.matrix++,
        jobRoleId,
        epiId,
        expectedCa: activeCaNumber(db, epiId) || '—',
        assignment: 'INDIVIDUAL',
        trainingRequired: false,
      };
      db.matrix.push(row);
      api.audit('MATRIX_ADD', 'Função #' + jobRoleId + ' · EPI #' + epiId);
      persist(db);
      return row;
    },
    removeMatrixRow(id) {
      const db = loadDb();
      const row = db.matrix.find(m => m.id === id);
      db.matrix = db.matrix.filter(m => m.id !== id);
      if (row) api.audit('MATRIX_REMOVE', 'Função #' + row.jobRoleId + ' · EPI #' + row.epiId);
      persist(db);
    },
    updateMatrixRow(id, patch) {
      const db = loadDb();
      const m = db.matrix.find(x => x.id === id);
      if (!m) throw new Error('Linha da matriz não encontrada.');
      if (patch.assignment === 'INDIVIDUAL' || patch.assignment === 'POSTO') m.assignment = patch.assignment;
      if (typeof patch.trainingRequired === 'boolean') m.trainingRequired = patch.trainingRequired;
      if (patch.expectedCa !== undefined) m.expectedCa = (patch.expectedCa || '').trim() || '—';
      if (patch.refreshCa) m.expectedCa = activeCaNumber(db, m.epiId) || m.expectedCa;
      persist(db);
      return m;
    },
    listJobRolesForMatrix() {
      const db = loadDb();
      return db.jobRoles.filter(j => j.active).map(j => {
        const dep = db.departments.find(d => d.id === j.departmentId);
        return { ...j, departmentName: dep ? dep.name : '—', label: j.name + ' — ' + (dep ? dep.name : '—') };
      });
    },
    episAvailableForMatrix(jobRoleId) {
      const db = loadDb();
      const inMatrix = db.matrix.filter(m => m.jobRoleId === jobRoleId).map(m => m.epiId);
      return db.epis.filter(ep => ep.active && !inMatrix.includes(ep.id));
    },
    getPeriodicity(epiId) {
      const p = loadDb().periodicity.find(x => x.epiId === epiId);
      return p ? p.days : null;
    },
    getPeriodicityEntry(epiId) {
      const p = loadDb().periodicity.find(x => x.epiId === epiId);
      return p ? { ...p } : { epiId, days: 180, warnDays: 15 };
    },
    listPeriodicityRows() {
      const db = loadDb();
      return db.matrix.map(m => {
        const job = db.jobRoles.find(j => j.id === m.jobRoleId);
        const ep = epiById(db, m.epiId);
        const per = api.getPeriodicityEntry(m.epiId);
        return {
          matrixId: m.id,
          jobRoleId: m.jobRoleId,
          jobRoleName: job ? job.name : '—',
          epiId: m.epiId,
          epiName: ep ? ep.description : '—',
          days: per.days,
          warnDays: per.warnDays != null ? per.warnDays : 15,
        };
      });
    },
    setPeriodicity(epiId, days, warnDays) {
      const db = loadDb();
      days = parseInt(days, 10);
      if (!days || days < 1) throw new Error('Periodicidade inválida.');
      warnDays = warnDays != null ? parseInt(warnDays, 10) : 15;
      if (warnDays < 0 || warnDays >= days) throw new Error('Aviso antecipado deve ser menor que a periodicidade.');
      let p = db.periodicity.find(x => x.epiId === epiId);
      if (p) {
        p.days = days;
        p.warnDays = warnDays;
      } else db.periodicity.push({ epiId, days, warnDays });
      api.audit('PERIODICITY_SET', 'EPI #' + epiId + ' · ' + days + ' dias · aviso ' + warnDays + 'd');
      persist(db);
    },
    periodicityLabel(epiId) {
      const d = api.getPeriodicity(epiId);
      if (!d) return '—';
      if (d >= 365) return Math.round(d / 365) + ' ano(s)';
      if (d >= 30) return Math.round(d / 30) + ' mes(es)';
      return d + ' dias';
    },

    /* --- entregas --- */
    getIssuances(filter) {
      const db = loadDb();
      const q = norm(filter && filter.q);
      let rows = db.issuances.slice();
      if (filter && filter.employeeId) rows = rows.filter(r => r.employeeId === filter.employeeId);
      if (filter && filter.status) rows = rows.filter(r => r.status === filter.status);
      if (q) {
        rows = rows.filter(r => {
          const resolved = resolveIssuance(r);
          return norm(r.code).includes(q) || norm(resolved.employeeCode).includes(q) ||
            norm(resolved.employeeName).includes(q);
        });
      }
      return rows.map(resolveIssuance).sort((a, b) => (a.issuedAt < b.issuedAt ? 1 : -1));
    },
    findIssuanceByCode(code) {
      const c = (code || '').trim();
      const row = loadDb().issuances.find(i => i.code === c);
      return row ? resolveIssuance(row) : null;
    },
    createIssuance(payload) {
      const db = loadDb();
      const emp = db.employees.find(e => e.id === payload.employeeId);
      if (!emp || !emp.active) throw new Error('Trabalhador inativo ou não encontrado.');
      const ep = epiById(db, payload.epiId);
      if (!ep || !ep.active) throw new Error('EPI inativo ou indisponível.');
      const lot = db.lots.find(l => l.id === payload.lotId);
      if (!lot || lot.epiId !== ep.id) throw new Error('Lote inválido para este EPI.');
      if (lotExpired(lot)) throw new Error('Lote vencido — fornecimento bloqueado (NR-6 6.9.2.1.1).');
      const qty = parseInt(payload.qty, 10) || 1;
      if (lot.qtyOnHand < qty) throw new Error('Saldo insuficiente no lote.');
      if (!payload.awareness || !payload.termAccepted) throw new Error('Ciência e termo são obrigatórios.');
      const inMatrix = db.matrix.some(m => m.jobRoleId === emp.jobRoleId && m.epiId === ep.id);
      if (!inMatrix && !payload.matrixException) {
        throw new Error('Item fora da matriz — registre exceção SESMT.');
      }
      const caNumber = activeCaNumber(db, ep.id) || lot.caNumber;
      const id = db.nextId.issuance++;
      const code = 'ENT-2026-' + String(8800 + id);
      const row = {
        id,
        code,
        employeeId: emp.id,
        epiId: ep.id,
        lotId: lot.id,
        caNumber,
        qty,
        issuedAt: new Date().toISOString(),
        status: 'ACTIVE',
        awareness: true,
        termAccepted: true,
        matrixException: !!payload.matrixException,
        operatorLogin: payload.operatorLogin || api.getSessionUser().login,
        requestId: payload.requestId || null,
      };
      lot.qtyOnHand -= qty;
      db.issuances.push(row);
      if (payload.requestId) {
        const req = db.requests.find(r => r.id === payload.requestId);
        if (req) req.status = 'FULFILLED';
      }
      const er = api.resolveEmployeeRow(emp);
      api.audit('ISSUANCE_CREATE', code + ' · ' + emp.fullName + ' · ' + ep.description);
      persist(db);
      return resolveIssuance(row);
    },

    createReturn(issuanceId, payload) {
      const db = loadDb();
      const iss = db.issuances.find(i => i.id === issuanceId);
      if (!iss || iss.status !== 'ACTIVE') throw new Error('Entrega não disponível para devolução.');
      const retDate = (payload.returnedAt || '').slice(0, 10);
      const entDate = iss.issuedAt.slice(0, 10);
      if (retDate < entDate) throw new Error('Data anterior à entrega — bloqueado.');
      const id = db.nextId.return++;
      const code = 'DEV-2026-' + String(100 + id);
      db.returns.push({
        id, code, issuanceId, returnedAt: payload.returnedAt || new Date().toISOString(),
        reason: payload.reason || 'Devolução',
      });
      iss.status = 'RETURNED';
      const lot = db.lots.find(l => l.id === iss.lotId);
      if (lot) lot.qtyOnHand += iss.qty;
      api.audit('RETURN_CREATE', code + ' ref ' + iss.code);
      persist(db);
      return code;
    },

    createReversal(issuanceCode, reason) {
      const db = loadDb();
      reason = (reason || '').trim();
      if (reason.length < 10) throw new Error('Motivo obrigatório (mín. 10 caracteres).');
      const iss = db.issuances.find(i => i.code === issuanceCode);
      if (!iss || iss.status !== 'ACTIVE') throw new Error('Registro não estornável (já devolvido/estornado).');
      const id = db.nextId.reversal++;
      const code = 'EST-2026-' + String(10 + id);
      db.reversals.push({
        id, code, issuanceId: iss.id, reversedAt: new Date().toISOString(), reason,
      });
      iss.status = 'REVERSED';
      const lot = db.lots.find(l => l.id === iss.lotId);
      if (lot) lot.qtyOnHand += iss.qty;
      api.audit('REVERSAL_CREATE', code + ' ref ' + iss.code + ' — ' + reason.slice(0, 80));
      persist(db);
      return code;
    },

    /* --- solicitações --- */
    getRequests(filter) {
      const db = loadDb();
      let rows = db.requests.slice();
      if (filter && filter.status) rows = rows.filter(r => r.status === filter.status);
      if (filter && filter.employeeId) rows = rows.filter(r => r.employeeId === filter.employeeId);
      return rows.map(r => {
        const emp = db.employees.find(e => e.id === r.employeeId);
        const ep = epiById(db, r.epiId);
        return {
          ...r,
          employeeName: emp ? emp.fullName : '—',
          epiName: ep ? ep.description : '—',
        };
      }).sort((a, b) => (a.requestedAt < b.requestedAt ? 1 : -1));
    },
    createRequest(payload) {
      const db = loadDb();
      const emp = db.employees.find(e => e.id === payload.employeeId);
      if (!emp || !emp.active) throw new Error('Trabalhador inválido.');
      const epiId = payload.epiId;
      const open = db.requests.some(r =>
        r.employeeId === emp.id && r.epiId === epiId && ['PENDING', 'APPROVED', 'PARTIAL'].includes(r.status)
      );
      if (open) throw new Error('Já existe pedido em aberto para este EPI.');
      const inMatrix = db.matrix.some(m => m.jobRoleId === emp.jobRoleId && m.epiId === epiId);
      const id = db.nextId.request++;
      const row = {
        id,
        employeeId: emp.id,
        epiId,
        qty: parseInt(payload.qty, 10) || 1,
        reason: payload.reason || '—',
        status: inMatrix ? 'PENDING' : 'PENDING_SESMT',
        requestedAt: new Date().toISOString(),
        requestedBy: api.getSessionUser().login,
        notes: payload.notes || '',
      };
      db.requests.push(row);
      api.audit('REQUEST_CREATE', '#' + id + ' · ' + emp.fullName);
      persist(db);
      return row;
    },
    setRequestStatus(id, status) {
      const db = loadDb();
      const r = db.requests.find(x => x.id === id);
      if (!r) throw new Error('Solicitação não encontrada.');
      r.status = status;
      persist(db);
      return r;
    },

    /* --- cobertura / dashboard --- */
    coverageForEmployee(employeeId) {
      const db = loadDb();
      const emp = db.employees.find(e => e.id === employeeId);
      if (!emp) return [];
      const matrix = db.matrix.filter(m => m.jobRoleId === emp.jobRoleId);
      return matrix.map(m => {
        const ep = epiById(db, m.epiId);
        const last = db.issuances
          .filter(i => i.employeeId === employeeId && i.epiId === m.epiId && i.status === 'ACTIVE')
          .sort((a, b) => (a.issuedAt < b.issuedAt ? 1 : -1))[0];
        const per = api.getPeriodicityEntry(m.epiId);
        const days = per.days || 180;
        const warn = per.warnDays != null ? per.warnDays : 15;
        let status = 'Pendente';
        let badge = 'badge-danger';
        if (last) {
          const age = (Date.now() - new Date(last.issuedAt).getTime()) / 86400000;
          const remaining = days - age;
          if (remaining > warn) { status = 'Vigente'; badge = 'badge-ok'; }
          else if (remaining > 0) { status = 'Troca ' + Math.ceil(remaining) + 'd'; badge = 'badge-warn'; }
          else { status = 'Vencido'; badge = 'badge-danger'; }
        }
        return {
          epiId: m.epiId,
          epiName: ep ? ep.description : '—',
          status,
          badge,
        };
      });
    },
    getCoverageReport(filter) {
      const q = norm(filter && filter.q);
      const statusFilter = (filter && filter.status) || 'all';
      const db = loadDb();
      let rows = db.employees.filter(e => e.active).map(e => {
        const er = api.resolveEmployeeRow(e);
        const cov = api.coverageForEmployee(e.id);
        const required = cov.length;
        const vigente = cov.filter(c => c.status === 'Vigente').length;
        const pending = cov.filter(c => c.status === 'Pendente').length;
        const expiring = cov.filter(c => c.status === 'Vencido' || String(c.status).indexOf('Troca') === 0).length;
        let gapKind = 'ok';
        let gapLabel = 'OK';
        let gapBadge = 'badge-ok';
        if (required === 0) {
          gapKind = 'sem_matriz';
          gapLabel = 'Sem matriz';
          gapBadge = 'badge-muted';
        } else if (pending > 0) {
          gapKind = 'descoberto';
          gapLabel = pending + ' pendente' + (pending > 1 ? 's' : '');
          gapBadge = 'badge-danger';
        } else if (expiring > 0) {
          gapKind = 'vencendo';
          gapLabel = expiring + ' vencendo';
          gapBadge = 'badge-warn';
        }
        return {
          employeeId: e.id,
          employeeCode: e.employeeCode,
          employeeName: e.fullName,
          jobRoleName: er.jobRoleName,
          departmentName: er.departmentName,
          required,
          vigente,
          pending,
          expiring,
          gapKind,
          gapLabel,
          gapBadge,
          items: cov,
        };
      });
      if (q) {
        rows = rows.filter(r =>
          norm(r.employeeName).includes(q) ||
          r.employeeCode.includes(q) ||
          norm(r.jobRoleName).includes(q) ||
          norm(r.departmentName).includes(q)
        );
      }
      if (statusFilter === 'descoberto') rows = rows.filter(r => r.pending > 0);
      else if (statusFilter === 'vencendo') rows = rows.filter(r => r.expiring > 0 && r.pending === 0);
      else if (statusFilter === 'ok') rows = rows.filter(r => r.gapKind === 'ok');
      return rows.sort((a, b) => norm(a.employeeName).localeCompare(norm(b.employeeName)));
    },
    getPendencies() {
      const db = loadDb();
      const items = [];
      function fmtDay(iso) {
        if (!iso) return '—';
        const d = new Date(iso);
        return isNaN(d) ? '—' : d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
      }

      db.employees.filter(e => !e.active).forEach(e => {
        const activeIss = db.issuances.filter(i => i.employeeId === e.id && i.status === 'ACTIVE');
        if (!activeIss.length) return;
        const since = activeIss.map(i => i.issuedAt).sort().reverse()[0];
        items.push({
          type: 'Devolução',
          employeeName: e.fullName,
          detail: 'Desligamento / inativo — ' + activeIss.length + ' item(ns) entregue(s) sem devolução',
          since: fmtDay(since),
          sinceSort: since,
          priority: 'Alta',
          priorityBadge: 'badge-danger',
          link: '03-devolucao.html?q=' + encodeURIComponent(e.employeeCode),
        });
      });

      db.employees.filter(e => e.active).forEach(e => {
        api.coverageForEmployee(e.id).forEach(c => {
          if (c.status === 'Vigente') return;
          const isPending = c.status === 'Pendente';
          items.push({
            type: isPending ? 'Descoberto (matriz)' : 'Troca periodicidade',
            employeeName: e.fullName,
            detail: c.epiName + ' — ' + c.status,
            since: '—',
            sinceSort: '',
            priority: isPending || c.status === 'Vencido' ? 'Alta' : 'Média',
            priorityBadge: isPending || c.status === 'Vencido' ? 'badge-danger' : 'badge-warn',
            link: '02-entrega-wizard.html',
          });
        });
      });

      db.requests.filter(r => r.status === 'PENDING_SESMT').forEach(r => {
        const emp = db.employees.find(e => e.id === r.employeeId);
        const ep = epiById(db, r.epiId);
        items.push({
          type: 'Exceção matriz',
          employeeName: emp ? emp.fullName : '—',
          detail: 'Pedido #' + r.id + ' · ' + (ep ? ep.description : 'EPI') + ' aguardando SESMT',
          since: fmtDay(r.requestedAt),
          sinceSort: r.requestedAt,
          priority: 'Média',
          priorityBadge: 'badge-warn',
          link: '17-fila-solicitacoes.html',
        });
      });

      db.requests.filter(r => r.status === 'APPROVED').forEach(r => {
        const emp = db.employees.find(e => e.id === r.employeeId);
        const ep = epiById(db, r.epiId);
        items.push({
          type: 'Atendimento fila',
          employeeName: emp ? emp.fullName : '—',
          detail: 'Pedido #' + r.id + ' aprovado · ' + (ep ? ep.description : 'EPI'),
          since: fmtDay(r.requestedAt),
          sinceSort: r.requestedAt,
          priority: 'Média',
          priorityBadge: 'badge-info',
          link: '02-entrega-wizard.html?request=' + r.id,
        });
      });

      const prOrder = { Alta: 0, Média: 1, Baixa: 2 };
      items.sort((a, b) => {
        const p = (prOrder[a.priority] || 9) - (prOrder[b.priority] || 9);
        if (p !== 0) return p;
        return (b.sinceSort || '').localeCompare(a.sinceSort || '');
      });
      return items;
    },
    dashboardMetrics(papel) {
      const db = loadDb();
      const today = todayStr();
      const issToday = db.issuances.filter(i => i.issuedAt.slice(0, 10) === today).length;
      const pendingReq = db.requests.filter(r => ['PENDING', 'APPROVED', 'PENDING_SESMT'].includes(r.status)).length;
      const lotsWarn = db.lots.filter(l => {
        if (lotExpired(l)) return false;
        const diff = (new Date(l.pieceValidUntil) - new Date(today)) / 86400000;
        return diff >= 0 && diff <= 30;
      }).length;
      const caepiBad = !db.caepi.valid;
      if (papel === 'Gestor') {
        return [['' + pendingReq, 'Pedidos em aberto', '17-fila-solicitacoes.html']];
      }
      if (papel === 'Consulta') {
        return [
          ['' + db.issuances.length, 'Entregas registradas', '05-historico-trabalhador.html'],
          ['' + db.auditLog.length, 'Eventos auditoria', '18-auditoria.html'],
        ];
      }
      if (papel === 'SESMT') {
        const descobertos = api.getCoverageReport({ status: 'descoberto' }).length;
        return [
          ['' + descobertos, 'Trabalhadores descobertos', '14-cobertura.html'],
          ['' + pendingReq, 'Fila demanda', '17-fila-solicitacoes.html'],
          [caepiBad ? '!' : 'OK', 'Status CAEPI', '21-caepi-import.html'],
        ];
      }
      if (papel === 'Admin') {
        const userAttn = api.countUsersNeedingAttention();
        return [
          ['' + issToday, 'Entregas hoje', '02-entrega-wizard.html'],
          ['' + pendingReq, 'Solicitações abertas', '17-fila-solicitacoes.html'],
          ['' + userAttn, 'Usuários atenção', '19-admin-usuarios.html'],
          [caepiBad ? '!' : 'OK', 'CAEPI', '21-caepi-import.html'],
        ];
      }
      return [
        ['' + pendingReq, 'Solicitações aprovadas/fila', '17-fila-solicitacoes.html'],
        ['' + lotsWarn, 'Lotes vencendo (30d)', '10-estoque-lotes.html'],
        ['' + issToday, 'Entregas hoje', '05-historico-trabalhador.html'],
      ];
    },
  };

  global.Enr6MockStore = api;
})(typeof window !== 'undefined' ? window : globalThis);
