#include "net_ssehub_kernel_haven_cnf_CryptoMiniSatSolver.h"

#include <cryptominisat5/cryptominisat.h>
#include <vector>

JNIEXPORT jboolean JNICALL Java_net_ssehub_kernel_1haven_cnf_CryptoMiniSatSolver_isSatisfiableImpl(
		JNIEnv *env, jclass clazz, jint numVars, jobjectArray clauses) {

	CMSat::SATSolver solver;

	solver.new_vars(numVars);

	jsize length = env->GetArrayLength(clauses);

	for (int i = 0; i < length; i++) {
		jintArray line = (jintArray) env->GetObjectArrayElement(clauses, i);
		jsize lineLength = env->GetArrayLength(line);
		jint *lineElements = env->GetIntArrayElements(line, 0);

		std::vector<CMSat::Lit> clause;

		for (int j = 0; j < lineLength; j++) {
			jint element = lineElements[j];

			bool negated = false;
			if (element < 0) {
				negated = true;
				element *= -1;
			}

			clause.push_back(CMSat::Lit(element - 1, negated));
		}

		solver.add_clause(clause);
	}

	CMSat::lbool result = solver.solve();
	return result == CMSat::l_True ? true : false;
}
