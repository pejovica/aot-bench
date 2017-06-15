import mx

_suite = mx.suite('aot-bench')

jdk = mx.get_jdk()
jdk.aot_image = mx.exe_suffix(mx.join(jdk.home, 'bin', 'aot-image'))

_bench_suite = {
    'fannkuchredux': [('15000', '10', '9'), ['-H:+MultiThreaded']],
    'mandelbrot': [('15000', '10', '500'), ['-H:+MultiThreaded']],
    'binarytrees': [('15000', '10', '14'), ['-H:+MultiThreaded']],
    'nbody': [('10000', '10', '100000'), []],
    # Throws NPE at the moment
    # 'javac': [('1000', '10'), [
    #     '-H:+ReportUnsupportedElementsAtRuntime',
    #     '-H:IncludeResourceBundles=com.sun.tools.javac.resources.compiler,com.sun.tools.javac.resources.javac,com.sun.tools.javac.resources.version',
    # ]],
    'spectralnorm': [('5000', '10', '500'), ['-H:+MultiThreaded']],
    'pidigits': [('7500', '50', '500'), []],
    'scalariform': [('10000', '10'), []],
    'chameneosredux': [('10000', '10', '12000'), ['-H:+MultiThreaded']],
}


class Bench(object):
    def __init__(self, src_file, class_file, classpath, runtime_args, build_args):
        self.src_file = src_file
        self.class_file = class_file
        self.classpath = classpath
        self.runtime_args = runtime_args
        self.build_args = build_args


def _collect_bench_data(bench_suite):
    benchmarks = {}
    for bench in bench_suite:
        for project in _suite.projects:
            try:
                bench_class, (bench_src, _) = project.find_classes_with_annotations(None, [
                    '@AOTBench("{}")'.format(bench)]).popitem()
            except KeyError:
                continue
            benchmarks[bench] = Bench(bench_src, bench_class, mx.classpath(names=project, jdk=jdk), *bench_suite[bench])
            break
        else:
            raise ValueError("Could not find a '{}' benchmark. Check the name in java sources.".format(bench))
    return benchmarks


def _build_aot_images(benchmarks):
    print('---------------- BEGIN BUILDING AOT IMAGES ----------------')
    output_dir = mx.join(_suite.get_output_root(), 'bin')
    mx.ensure_dir_exists(output_dir)
    for bench_name, bench in benchmarks.items():
        witness = mx.TimeStampFile(mx.join(output_dir, bench_name))
        if not witness.exists() or witness.isOlderThan(bench.src_file):
            cmd = [jdk.aot_image, '-cp', bench.classpath, '-H:Name={}'.format(bench_name),
                   '-H:Class={}'.format(bench.class_file)]
            cmd.extend(bench.build_args)
            print(' '.join(cmd))
            mx.run(cmd, cwd=output_dir)
    print('---------------- END BUILDING AOT IMAGES ----------------')


def _run(benchmarks):
    print('---------------- BEGIN RUNNING AOT BENCHMARKS ----------------')
    for bench_name, bench in benchmarks.items():
        common_args = ['-XX:+PrintGC']
        print('------------ BEGIN {} BENCHMARK ------------'.format(bench_name.upper()))
        print('-------- BEGIN GRAAL RUN --------')
        graal_cmd = [jdk.java, '-cp', bench.classpath] + common_args
        graal_cmd.append(bench.class_file)
        graal_cmd.extend(bench.runtime_args)
        print(' '.join(graal_cmd))
        mx.run(graal_cmd, cwd=_suite.get_output_root())
        print('-------- END GRAAL RUN --------')
        print('-------- BEGIN SVM RUN --------')
        svm_cmd = [mx.join('bin', bench_name)] + common_args
        svm_cmd.append('10')  # set fixed number of warm up rounds
        svm_cmd.extend(bench.runtime_args[1:])
        print(' '.join(svm_cmd))
        mx.run(svm_cmd, cwd=_suite.get_output_root())
        print('-------- END SVM RUN --------')
        print('------------ END {} BENCHMARK ------------'.format(bench_name.upper()))
    print('---------------- END RUNNING AOT BENCHMARKS ----------------')


def aot_benchmark(args):
    for bench in args:
        if bench not in _bench_suite:
            raise ValueError("'{}' is not an AOT benchmark.".format(bench))

    benchmarks = _collect_bench_data({bench: _bench_suite[bench] for bench in args} if args else _bench_suite)
    _build_aot_images(benchmarks)
    _run(benchmarks)


mx.update_commands(_suite, {
    'aot-benchmark': [aot_benchmark, '']
})
