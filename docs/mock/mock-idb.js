/**
 * IndexedDB — cadastros operacionais (store app) + índice CAEPI (store ca).
 */
(function (global) {
  const DB_NAME = 'enr6_mock_v1';
  const DB_VERSION = 1;
  const STORE_APP = 'app';
  const STORE_CA = 'ca';
  const APP_KEY = 'main';

  function openDb() {
    return new Promise(function (resolve, reject) {
      if (!global.indexedDB) {
        reject(new Error('IndexedDB não disponível neste navegador.'));
        return;
      }
      const req = global.indexedDB.open(DB_NAME, DB_VERSION);
      req.onupgradeneeded = function () {
        const db = req.result;
        if (!db.objectStoreNames.contains(STORE_APP)) {
          db.createObjectStore(STORE_APP, { keyPath: 'id' });
        }
        if (!db.objectStoreNames.contains(STORE_CA)) {
          db.createObjectStore(STORE_CA, { keyPath: 'caNumber' });
        }
      };
      req.onsuccess = function () { resolve(req.result); };
      req.onerror = function () { reject(req.error || new Error('Falha ao abrir IndexedDB')); };
    });
  }

  function saveApp(dbPayload) {
    return openDb().then(function (db) {
      return new Promise(function (resolve, reject) {
        const tx = db.transaction(STORE_APP, 'readwrite');
        tx.objectStore(STORE_APP).put({
          id: APP_KEY,
          payload: dbPayload,
          updatedAt: new Date().toISOString(),
        });
        tx.oncomplete = function () { db.close(); resolve(); };
        tx.onerror = function () { db.close(); reject(tx.error); };
      });
    });
  }

  function loadApp() {
    return openDb().then(function (db) {
      return new Promise(function (resolve, reject) {
        const tx = db.transaction(STORE_APP, 'readonly');
        const req = tx.objectStore(STORE_APP).get(APP_KEY);
        req.onsuccess = function () {
          db.close();
          resolve(req.result ? req.result.payload : null);
        };
        req.onerror = function () { db.close(); reject(req.error); };
      });
    });
  }

  function replaceCaIndex(index, onProgress) {
    const entries = Object.values(index || {});
    return openDb().then(function (db) {
      return new Promise(function (resolve, reject) {
        const tx = db.transaction(STORE_CA, 'readwrite');
        const store = tx.objectStore(STORE_CA);
        store.clear();
        let n = 0;
        entries.forEach(function (entry) {
          store.put(entry);
          n++;
          if (onProgress && n % 8000 === 0) {
            onProgress({ phase: 'idb', written: n, total: entries.length });
          }
        });
        tx.oncomplete = function () {
          if (onProgress) onProgress({ phase: 'idb', written: entries.length, total: entries.length, done: true });
          db.close();
          resolve(entries.length);
        };
        tx.onerror = function () { db.close(); reject(tx.error); };
      });
    });
  }

  function loadCaIndex(onProgress) {
    return openDb().then(function (db) {
      return new Promise(function (resolve, reject) {
        const tx = db.transaction(STORE_CA, 'readonly');
        const req = tx.objectStore(STORE_CA).openCursor();
        const index = {};
        let n = 0;
        req.onsuccess = function (ev) {
          const cursor = ev.target.result;
          if (cursor) {
            index[cursor.value.caNumber] = cursor.value;
            n++;
            if (onProgress && n % 8000 === 0) onProgress({ phase: 'hydrate', loaded: n });
            cursor.continue();
          } else {
            if (onProgress) onProgress({ phase: 'hydrate', loaded: n, done: true });
            db.close();
            resolve(index);
          }
        };
        req.onerror = function () { db.close(); reject(req.error); };
      });
    });
  }

  function getOneCa(caNumber) {
    return openDb().then(function (db) {
      return new Promise(function (resolve, reject) {
        const tx = db.transaction(STORE_CA, 'readonly');
        const req = tx.objectStore(STORE_CA).get(String(caNumber));
        req.onsuccess = function () { db.close(); resolve(req.result || null); };
        req.onerror = function () { db.close(); reject(req.error); };
      });
    });
  }

  function countCa() {
    return openDb().then(function (db) {
      return new Promise(function (resolve, reject) {
        const tx = db.transaction(STORE_CA, 'readonly');
        const req = tx.objectStore(STORE_CA).count();
        req.onsuccess = function () { db.close(); resolve(req.result || 0); };
        req.onerror = function () { db.close(); reject(req.error); };
      });
    });
  }

  function clearAll() {
    return openDb().then(function (db) {
      return new Promise(function (resolve, reject) {
        const tx = db.transaction([STORE_APP, STORE_CA], 'readwrite');
        tx.objectStore(STORE_APP).clear();
        tx.objectStore(STORE_CA).clear();
        tx.oncomplete = function () { db.close(); resolve(); };
        tx.onerror = function () { db.close(); reject(tx.error); };
      });
    });
  }

  /** Migração única: base antiga só-CAEPI (nome legado). */
  function migrateLegacyCaepiDbIfNeeded() {
    const LEGACY = 'enr6_mock_caepi_v1';
    return new Promise(function (resolve) {
      if (!global.indexedDB) { resolve(null); return; }
      const req = global.indexedDB.open(LEGACY, 1);
      req.onsuccess = function () {
        const old = req.result;
        if (!old.objectStoreNames.contains('ca')) {
          old.close();
          resolve(null);
          return;
        }
        const tx = old.transaction('ca', 'readonly');
        const cur = tx.objectStore('ca').openCursor();
        const index = {};
        cur.onsuccess = function (ev) {
          const c = ev.target.result;
          if (c) {
            index[c.value.caNumber] = c.value;
            c.continue();
          } else {
            old.close();
            resolve(Object.keys(index).length ? index : null);
          }
        };
        cur.onerror = function () { old.close(); resolve(null); };
      };
      req.onerror = function () { resolve(null); };
    });
  }

  global.Enr6MockIdb = {
    saveApp,
    loadApp,
    replaceCaIndex,
    loadCaIndex,
    getOneCa,
    countCa,
    clearAll,
    migrateLegacyCaepiDbIfNeeded,
    DB_NAME,
  };

  global.Enr6CaepiIdb = {
    replaceIndex: replaceCaIndex,
    loadAll: loadCaIndex,
    getOne: getOneCa,
    count: countCa,
    clearStore: clearAll,
    DB_NAME,
  };
})(typeof window !== 'undefined' ? window : globalThis);
