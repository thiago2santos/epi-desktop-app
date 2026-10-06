package br.com.easynr6.gestaoepi.modules.employee.application.port;

import java.util.List;
import java.util.Optional;

public interface GheRepository {

  boolean unitExists(Long unitId);

  Optional<GheStored> findById(Long gheId);

  boolean existsNameInUnit(String name, Long unitId, Long excludeId);

  Long create(Long unitId, String name, boolean active);

  void updateName(Long gheId, String name);

  void setActive(Long gheId, boolean active);

  Optional<FuncaoParaGhe> findJobRole(Long jobRoleId);

  boolean link(Long gheId, Long jobRoleId);

  boolean unlink(Long gheId, Long jobRoleId);

  Optional<GheMembership> findMembership(Long jobRoleId);

  List<GheSummary> listByUnit(Long unitId, String term);

  List<FuncaoDoGhe> listMembers(Long gheId);

  List<FuncaoDoGhe> listCandidates(Long unitId);

  record GheStored(Long id, Long unitId, String name, boolean active) {}

  record GheSummary(Long id, Long unitId, String name, boolean active, int funcoes) {}

  record FuncaoParaGhe(Long id, boolean active, Long unitId, Long gheId) {}

  record FuncaoDoGhe(Long id, String name, String departmentName, boolean active) {
    public String rotulo() {
      return departmentName + " — " + name;
    }
  }

  record GheMembership(Long gheId, boolean active) {}
}
