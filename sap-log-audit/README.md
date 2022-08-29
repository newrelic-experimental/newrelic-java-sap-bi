  # SAP audit logging properties
  #  log_file_name defaults to audit.log and reference is absolute.
  #    file pattern for rollover will append dot-number, like audit.log.1
  #  log_size_limit defaults to 50M, value is in bytes but input as string
  #  log_file_count defaults to zero, so no rollover.  Integer is expected.
  #  ignores is a comma-delimited list.  Defaults to nothing ignored.  Beware of colons or commas embedded
  #    this is a "contains" relationship so a partial string is find, too narrow a spec may cause
  #    too much ignored, like "e" would ignore every string with an e in it
  SAP:
    auditlog:
      log_file_name: "./audit.log"
      log_size_limit: 50M
      log_file_count: 3
      ignores: AAE_REQUEST_MAPPING,processing local module

