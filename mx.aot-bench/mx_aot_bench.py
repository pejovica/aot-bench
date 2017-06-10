import mx

_suite = mx.suite('aot-bench')

jdk = mx.get_jdk()
jdk.aot_image = mx.exe_suffix(mx.join(jdk.home, 'bin', 'aot-image'))

_project_name = 'com.oracle.svm.bench.shootouts'

_benchmarks = {
    'fannkuchredux': [('15000', '10', '9'), ['-H:+MultiThreaded']],
    'mandelbrot': [('15000', '10', '500'), ['-H:+MultiThreaded']],
    'binarytrees': [('15000', '10', '14'), ['-H:+MultiThreaded']],
    'nbody': [('10000', '10', '100000'), []],
    # 'javac': [('5000', '10'), []],
    'spectralnorm': [('5000', '10', '500'), ['-H:+MultiThreaded']],
    'pidigits': [('7500', '50', '500'), []],
}


def _get_project(name):
    for p in _suite.projects:
        if p.name == name:
            return p
    raise ValueError("'{}' is not aot-bench suite's project.".format(name))


def _build_aot_images(project, benchmarks):
    print('---------------- BEGIN BUILDING AOT IMAGES ----------------')
    output_dir = project.output_dir()
    class_files = {}
    for bench in benchmarks:
        bench_class, (bench_src, _) = project.find_classes_with_annotations(None, [
            '@AOTBench("{}")'.format(bench)]).popitem()
        class_files[bench] = bench_class
        witness = mx.TimeStampFile(mx.join(output_dir, bench))
        if not witness.exists() or witness.isOlderThan(bench_src):
            cmd = [jdk.aot_image, '-cp', '.', '-H:Name={}'.format(bench), '-H:Class={}'.format(bench_class)]
            cmd.extend(benchmarks[bench][1])
            print(' '.join(cmd))
            mx.run(cmd, cwd=output_dir)
    print('---------------- END BUILDING AOT IMAGES ----------------')
    return class_files


def aot_benchmark(args):
    for bench in args:
        if bench not in _benchmarks:
            raise ValueError("'{}' is not an AOT benchmark.".format(bench))

    project = _get_project(_project_name)
    benchmarks = {bench: _benchmarks[bench] for bench in args} if args else _benchmarks
    class_files = _build_aot_images(project, benchmarks)
    print('---------------- BEGIN RUNNING AOT BENCHMARKS ----------------')
    for bench in benchmarks:
        common_args = ['-XX:+PrintGC']
        print('------------ BEGIN {} BENCHMARK ------------'.format(bench.upper()))
        print('-------- BEGIN GRAAL RUN --------')
        graal_cmd = [jdk.java] + common_args
        graal_cmd.append(class_files[bench])
        graal_cmd.extend(benchmarks[bench][0])
        print(' '.join(graal_cmd))
        mx.run(graal_cmd, cwd=project.output_dir())
        print('-------- END GRAAL RUN --------')
        print('-------- BEGIN SVM RUN --------')
        svm_cmd = [mx.join('.', bench)] + common_args
        svm_cmd.append('10')  # set fixed number of warm up rounds
        svm_cmd.extend(benchmarks[bench][0][1:])
        print(' '.join(svm_cmd))
        mx.run(svm_cmd, cwd=project.output_dir())
        print('-------- END SVM RUN --------')
        print('------------ END {} BENCHMARK ------------'.format(bench.upper()))
    print('---------------- END RUNNING AOT BENCHMARKS ----------------')


mx.update_commands(_suite, {
    'aot-benchmark': [aot_benchmark, '']
})
