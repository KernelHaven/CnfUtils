#include "net_ssehub_kernel_haven_cnf_CryptoMiniSatSolver.h"

#include <cryptominisat5/cryptominisat.h>
#include <vector>

JNIEXPORT jboolean JNICALL Java_net_ssehub_kernel_1haven_cnf_CryptoMiniSatSolver_isSatisfiableImpl(
	JNIEnv *env, jclass clazz, jint numVars, jint numClauses, jobject intBuffer) {

	CMSat::SATSolver solver;
	solver.new_vars(numVars);

	jint *buffer = (jint *) env->GetDirectBufferAddress(intBuffer);

	int bufferIndex = 0;
	std::vector<CMSat::Lit> clause;
	for (int i = 0; i < numClauses; i++) {

 		jint length = buffer[bufferIndex++];
		clause.reserve(length);

		for (int j = 0; j < length; j++) {
			jint element = buffer[bufferIndex++];

			bool negated = false;
			if (element < 0) {
				negated = true;
				element *= -1;
			}

			clause.push_back(CMSat::Lit(element - 1, negated));
		}

		solver.add_clause(clause);
		clause.clear();
	}

	CMSat::lbool result = solver.solve();

	return result == CMSat::l_True ? true : false;
}
