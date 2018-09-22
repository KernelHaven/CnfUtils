#include "net_ssehub_kernel_haven_cnf_CryptoMiniSatSolver.h"

#include <cryptominisat5/cryptominisat.h>
#include <vector>

/*
 * Helper method for throwing a new SolverException.
 * After this method is called, the JNI call should return so that the exception
 * is propagated to Java.
 */
static void throwSolverException(JNIEnv *env, const char *msg) {
	jclass solverExceptionClass = env->FindClass("net/ssehub/kernel_haven/cnf/SolverException");
	if (env->ExceptionCheck() || solverExceptionClass == nullptr) {
		// couldn't load SolverException class because of some exception
		// the pending exception will be thrown in Java
		return;
	}

	env->ThrowNew(solverExceptionClass, msg);
	// can't do anything useful if throwing fails (?)
}

/*
 * Checks if accessing the given index on a buffer with the given capacity
 * is out of bounds. If an overflow occurs, this method throws a SolverException
 * and returns true. The calling JNI call should return, so that the exception
 * is propagated to Java.
 */
static bool checkBufferOverflow(JNIEnv *env, jint capacaity, jint index) {
	if (index >= capacaity) {
		throwSolverException(env, "Buffer index out of bounds");
		return true;
	}
	return false;
}

JNIEXPORT jboolean JNICALL Java_net_ssehub_kernel_1haven_cnf_CryptoMiniSatSolver_isSatisfiableImpl(
		JNIEnv *env, jclass clazz, jint numVars, jint numClauses, jobject intBuffer) {

	/*
	 * Check parameters
	 */

	if (numVars <= 0) {
		throwSolverException(env, "numVars is <= 0");
		return false;
	}

	if (numClauses <= 0) {
		throwSolverException(env, "numClauses is <= 0");
		return false;
	}

	if (intBuffer == nullptr) {
		throwSolverException(env, "intBuffer is NULL");
		return false;
	}

	/*
	 * Get direct buffer
	 */

	jint *buffer = (jint *) env->GetDirectBufferAddress(intBuffer);
	if (buffer == nullptr) {
		throwSolverException(env, "GetDirectBufferAddress() returned NULL");
		return false;
	}
	jlong bufferCap = env->GetDirectBufferCapacity(intBuffer);
	if (bufferCap <= 0) {
		throwSolverException(env, "GetDirectBufferCapacity() returned <= 0");
		return false;
	}

	/*
	 * Create solver
	 */

	CMSat::SATSolver solver;
	try {
		solver.new_vars(numVars);
	} catch (CMSat::TooManyVarsError) {
		throwSolverException(env, "Got TooManyVarsError");
		return false;
	} catch (...) {
		throwSolverException(env, "Exception while allocating variables");
		return false;
	}

	/*
	 * Read clauses from buffer and add them to the solver
	 */

	jint bufferIndex = 0;
	std::vector<CMSat::Lit> clause;
	for (jint i = 0; i < numClauses; i++) {

		if (checkBufferOverflow(env, bufferCap, bufferIndex)) {
			return false;
		}
 		jint length = buffer[bufferIndex++];
		clause.reserve(length);

		for (jint j = 0; j < length; j++) {
			if (checkBufferOverflow(env, bufferCap, bufferIndex)) {
				return false;
			}
			jint element = buffer[bufferIndex++];

			jboolean negated = false;
			if (element < 0) {
				negated = true;
				element *= -1;
			}

			if (element > numVars) {
				throwSolverException(env, "Trying to add too high variable");
				return false;
			}

			clause.push_back(CMSat::Lit(element - 1, negated));
		}

		try {
			solver.add_clause(clause);
		} catch (CMSat::TooLongClauseError) {
			throwSolverException(env, "Got TooLongClauseError");
			return false;
		} catch (...) {
			throwSolverException(env, "Exception while adding clause");
			return false;
		}
		clause.clear();
	}

	/*
	 * Solve
	 */

	CMSat::lbool result;
	try {
		result = solver.solve();
	} catch (...) {
		throwSolverException(env, "Exception while solving");
		return false;
	}

	return result == CMSat::l_True ? true : false;
}
