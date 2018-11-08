custom_target('flyinn-fakeinputlib',
               build_by_default: true,
               build_always_stale: true,
               input: '.',
               output: 'flyinn-fakeinputlib.jar',
               command: [find_program('./scripts/build-wrapper.sh'), '@INPUT@', '@OUTPUT@', get_option('buildtype')],
               install: true,
               install_dir: 'share/fakeinputlib')
