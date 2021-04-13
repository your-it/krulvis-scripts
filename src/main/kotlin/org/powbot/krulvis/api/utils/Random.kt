package org.powbot.krulvis.api.utils

object Random {

    private var random: java.util.Random? = null    // pseudo-random number generator
    private var seed: Long = 0        // pseudo-random number generator seed

    // static initializer
    init {
        // this is how the seed was set in Java 1.4
        seed = System.currentTimeMillis()
        random = java.util.Random(seed)
    }

    fun nextBoolean(): Boolean {
        return random!!.nextBoolean()
    }

    /**
     * Sets the seed of the pseudorandom number generator.
     * This method enables you to produce the same sequence of "random"
     * number for each execution of the program.
     * Ordinarily, you should call this method at most once per program.
     *
     * @param s the seed
     */
    fun setSeed(s: Long) {
        seed = s
        random = java.util.Random(seed)
    }

    /**
     * Returns the seed of the pseudorandom number generator.
     *
     * @return the seed
     */
    fun getSeed(): Long {
        return seed
    }

    /**
     * Returns a random real number uniformly in [0, 1).
     *
     * @return a random real number uniformly in [0, 1)
     */
    fun uniform(): Double {
        return random!!.nextDouble()
    }

    /**
     * Returns a random integer uniformly in [0, n).
     *
     * @param n number of possible integers
     * @return a random integer uniformly between 0 (inclusive) and `n` (exclusive)
     * @throws IllegalArgumentException if `n <= 0`
     */
    fun uniform(n: Int): Int {
        if (n <= 0) throw IllegalArgumentException("argument must be positive")
        return random!!.nextInt(n)
    }

    ///////////////////////////////////////////////////////////////////////////
    //  STATIC METHODS BELOW RELY ON JAVA.UTIL.RANDOM ONLY INDIRECTLY VIA
    //  THE STATIC METHODS ABOVE.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns a random real number uniformly in [0, 1).
     *
     * @return a random real number uniformly in [0, 1)
     */
    @Deprecated("Replaced by {@link #uniform()}.")
    fun random(): Double {
        return uniform()
    }

    /**
     * Returns a random integer uniformly in [a, b).
     *
     * @param a the left endpoint
     * @param b the right endpoint
     * @return a random integer uniformly in [a, b)
     * @throws IllegalArgumentException if `b <= a`
     * @throws IllegalArgumentException if `b - a >= Integer.MAX_VALUE`
     */
    fun uniform(a: Int, b: Int): Int {
        if (b <= a || b.toLong() - a >= Integer.MAX_VALUE) {
            throw IllegalArgumentException("invalid range: [$a, $b]")
        }
        return a + uniform(b - a)
    }

    /**
     * Returns a random real number uniformly in [a, b).
     *
     * @param a the left endpoint
     * @param b the right endpoint
     * @return a random real number uniformly in [a, b)
     * @throws IllegalArgumentException unless `a < b`
     */
    fun uniform(a: Double, b: Double): Double {
        if (a >= b) {
            throw IllegalArgumentException("invalid range: [$a, $b]")
        }
        return a + uniform() * (b - a)
    }

    /**
     * Returns a random boolean from a Bernoulli distribution with success
     * probability *p*.
     *
     * @param p the probability of returning `true`
     * @return `true` with probability `p` and
     * `false` with probability `p`
     * @throws IllegalArgumentException unless `0`  `p`  `1.0`
     */
    @JvmOverloads
    fun bernoulli(p: Double = 0.5): Boolean {
        if (!(p >= 0.0 && p <= 1.0))
            throw IllegalArgumentException("probability p must be between 0.0 and 1.0")
        return uniform() < p
    }

    /**
     * Returns a random real number from a standard Gaussian distribution.
     *
     * @return a random real number from a standard Gaussian distribution
     * (mean 0 and standard deviation 1).
     */
    fun gaussian(): Double {
        // use the polar form of the Box-Muller transform
        var r: Double
        var x: Double
        var y: Double
        do {
            x = uniform(-1.0, 1.0)
            y = uniform(-1.0, 1.0)
            r = x * x + y * y
        } while (r >= 1 || r == 0.0)
        return x * Math.sqrt(-2 * Math.log(r) / r)

        // Remark:  y * Math.sqrt(-2 * Math.log(r) / r)
        // is an independent random gaussian
    }

    /**
     * Returns a random real number from a Gaussian distribution with mean
     * and standard deviation .
     *
     * @param mean the mean
     * @param std  the standard deviation
     * @return a real number distributed according to the Gaussian distribution
     * with mean `mu` and standard deviation `sigma`
     */
    fun gaussian(mean: Double, std: Double): Double {
        return mean + std * gaussian()
    }

    fun nextGaussian(min: Int, max: Int): Int {
        if (max < min) {

        }
        val avg = (max - min) / 2 + min
        return nextGaussian(min, max, avg)
    }

    /**
     * Generates a pseudo-random number between the two given values with standard deviation.
     *
     * @param min The minimum value (inclusive).
     * @param max The maximum value (exclusive).
     * @param sd  Standard deviation.
     * @return The generated pseudo-random integer.
     */
    fun nextGaussian(min: Int, max: Int, sd: Int): Int {
        return nextGaussian(min, max, min + (max - min) / 2, sd)
    }

    /**
     * Generates a pseudo-random number between the two given values with standard deviation about a provided mean.
     *
     * @param min  The minimum value (inclusive).
     * @param max  The maximum value (exclusive).
     * @param mean The mean (>= min & < max).
     * @param sd   Standard deviation.
     * @return The generated pseudo-random integer.
     */
    fun nextGaussian(min: Int, max: Int, mean: Int, sd: Int): Int {
        if (min == max) {
            return min
        } else if (min > max) {
            throw NumberFormatException("Min must be higher than max")
        }
        var rand: Int
        do {
            rand = (gaussian() * sd + mean).toInt()
        } while (rand < min || rand >= max)
        return rand
    }

    /**
     * Regular shit random
     *
     * @param min
     * @param max
     * @return
     */
    fun nextInt(min: Int, max: Int): Int {
        return min + random!!.nextInt(max - min)
    }

    fun nextInt(max: Int): Int {
        return random!!.nextInt(max)
    }

    fun nextLong(pair: Pair<Long, Long>): Long {
        return nextInt(pair.first.toInt(), pair.second.toInt()).toLong()
    }


    /**
     * Returns a random integer from a geometric distribution with success
     * probability *p*.
     *
     * @param p the parameter of the geometric distribution
     * @return a random integer from a geometric distribution with success
     * probability `p`; or `Integer.MAX_VALUE` if
     * `p` is (nearly) equal to `1.0`.
     * @throws IllegalArgumentException unless `p >= 0.0` and `p <= 1.0`
     */
    fun geometric(p: Double): Int {
        if (!(p >= 0.0 && p <= 1.0)) {
            throw IllegalArgumentException("probability p must be between 0.0 and 1.0")
        }
        // using algorithm given by Knuth
        return Math.ceil(Math.log(uniform()) / Math.log(1.0 - p)).toInt()
    }

    /**
     * Returns a random integer from a Poisson distribution with mean .
     *
     * @param lambda the mean of the Poisson distribution
     * @return a random integer from a Poisson distribution with mean `lambda`
     * @throws IllegalArgumentException unless `lambda > 0.0` and not infinite
     */
    fun poisson(lambda: Double): Int {
        if (lambda <= 0.0)
            throw IllegalArgumentException("lambda must be positive")
        if (java.lang.Double.isInfinite(lambda))
            throw IllegalArgumentException("lambda must not be infinite")
        // using algorithm given by Knuth
        // see http://en.wikipedia.org/wiki/Poisson_distribution
        var k = 0
        var p = 1.0
        val expLambda = Math.exp(-lambda)
        do {
            k++
            p *= uniform()
        } while (p >= expLambda)
        return k - 1
    }

    /**
     * Returns a random real number from a Pareto distribution with
     * shape parameter .
     *
     * @param alpha shape parameter
     * @return a random real number from a Pareto distribution with shape
     * parameter `alpha`
     * @throws IllegalArgumentException unless `alpha > 0.0`
     */
    @JvmOverloads
    fun pareto(alpha: Double = 1.0): Double {
        if (alpha <= 0.0)
            throw IllegalArgumentException("alpha must be positive")
        return Math.pow(1 - uniform(), -1.0 / alpha) - 1.0
    }

    /**
     * Returns a random real number from the Cauchy distribution.
     *
     * @return a random real number from the Cauchy distribution.
     */
    fun cauchy(): Double {
        return Math.tan(Math.PI * (uniform() - 0.5))
    }

    /**
     * Returns a random integer from the specified discrete distribution.
     *
     * @param probabilities the probability of occurrence of each integer
     * @return a random integer from a discrete distribution:
     * `i` with probability `probabilities[i]`
     * @throws IllegalArgumentException if `probabilities` is `null`
     * @throws IllegalArgumentException if sum of array entries is not (very nearly) equal to `1.0`
     * @throws IllegalArgumentException unless `probabilities[i] >= 0.0` for each index `i`
     */
    fun discrete(probabilities: DoubleArray?): Int {
        if (probabilities == null) throw IllegalArgumentException("argument array is null")
        val EPSILON = 1E-14
        var sum = 0.0
        for (i in probabilities.indices) {
            if (probabilities[i] < 0.0)
                throw IllegalArgumentException("array entry " + i + " must be nonnegative: " + probabilities[i])
            sum += probabilities[i]
        }
        if (sum > 1.0 + EPSILON || sum < 1.0 - EPSILON)
            throw IllegalArgumentException("sum of array entries does not approximately equal 1.0: $sum")

        // the for loop may not return a value when both r is (nearly) 1.0 and when the
        // cumulative sum is less than 1.0 (as a result of floating-point roundoff error)
        while (true) {
            val r = uniform()
            sum = 0.0
            for (i in probabilities.indices) {
                sum = sum + probabilities[i]
                if (sum > r) return i
            }
        }
    }

    /**
     * Returns a random integer from the specified discrete distribution.
     *
     * @param frequencies the frequency of occurrence of each integer
     * @return a random integer from a discrete distribution:
     * `i` with probability proportional to `frequencies[i]`
     * @throws IllegalArgumentException if `frequencies` is `null`
     * @throws IllegalArgumentException if all array entries are `0`
     * @throws IllegalArgumentException if `frequencies[i]` is negative for any index `i`
     * @throws IllegalArgumentException if sum of frequencies exceeds `Integer.MAX_VALUE` (2<sup>31</sup> - 1)
     */
    fun discrete(frequencies: IntArray?): Int {
        if (frequencies == null) throw IllegalArgumentException("argument array is null")
        var sum: Long = 0
        for (i in frequencies.indices) {
            if (frequencies[i] < 0)
                throw IllegalArgumentException("array entry " + i + " must be nonnegative: " + frequencies[i])
            sum += frequencies[i].toLong()
        }
        if (sum == 0L)
            throw IllegalArgumentException("at least one array entry must be positive")
        if (sum >= Integer.MAX_VALUE)
            throw IllegalArgumentException("sum of frequencies overflows an int")

        // pick index i with probabilitity proportional to frequency
        val r = uniform(sum.toInt()).toDouble()
        sum = 0
        for (i in frequencies.indices) {
            sum += frequencies[i].toLong()
            if (sum > r) return i
        }

        // can't reach here
        assert(false)
        return -1
    }

    /**
     * Returns a random real number from an exponential distribution
     * with rate .
     *
     * @param lambda the rate of the exponential distribution
     * @return a random real number from an exponential distribution with
     * rate `lambda`
     * @throws IllegalArgumentException unless `lambda > 0.0`
     */
    fun exp(lambda: Double): Double {
        if (lambda <= 0.0)
            throw IllegalArgumentException("lambda must be positive")
        return -Math.log(1 - uniform()) / lambda
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param a the array to shuffle
     * @throws IllegalArgumentException if `a` is `null`
     */
    fun shuffle(a: Array<Any>?) {
        if (a == null) throw IllegalArgumentException("argument array is null")
        val n = a.size
        for (i in 0 until n) {
            val r = i + uniform(n - i)     // between i and n-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param a the array to shuffle
     * @throws IllegalArgumentException if `a` is `null`
     */
    fun shuffle(a: DoubleArray?) {
        if (a == null) throw IllegalArgumentException("argument array is null")
        val n = a.size
        for (i in 0 until n) {
            val r = i + uniform(n - i)     // between i and n-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param a the array to shuffle
     * @throws IllegalArgumentException if `a` is `null`
     */
    fun shuffle(a: IntArray?) {
        if (a == null) throw IllegalArgumentException("argument array is null")
        val n = a.size
        for (i in 0 until n) {
            val r = i + uniform(n - i)     // between i and n-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param a the array to shuffle
     * @throws IllegalArgumentException if `a` is `null`
     */
    fun shuffle(a: CharArray?) {
        if (a == null) throw IllegalArgumentException("argument array is null")
        val n = a.size
        for (i in 0 until n) {
            val r = i + uniform(n - i)     // between i and n-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified subarray in uniformly random order.
     *
     * @param a  the array to shuffle
     * @param lo the left endpoint (inclusive)
     * @param hi the right endpoint (exclusive)
     * @throws IllegalArgumentException  if `a` is `null`
     * @throws IndexOutOfBoundsException unless `(0 <= lo) && (lo < hi) && (hi <= a.length)`
     */
    fun shuffle(a: Array<Any>?, lo: Int, hi: Int) {
        if (a == null) throw IllegalArgumentException("argument array is null")
        if (lo < 0 || lo >= hi || hi > a.size) {
            throw IndexOutOfBoundsException("invalid subarray range: [$lo, $hi)")
        }
        for (i in lo until hi) {
            val r = i + uniform(hi - i)     // between i and hi-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified subarray in uniformly random order.
     *
     * @param a  the array to shuffle
     * @param lo the left endpoint (inclusive)
     * @param hi the right endpoint (exclusive)
     * @throws IllegalArgumentException  if `a` is `null`
     * @throws IndexOutOfBoundsException unless `(0 <= lo) && (lo < hi) && (hi <= a.length)`
     */
    fun shuffle(a: DoubleArray?, lo: Int, hi: Int) {
        if (a == null) throw IllegalArgumentException("argument array is null")
        if (lo < 0 || lo >= hi || hi > a.size) {
            throw IndexOutOfBoundsException("invalid subarray range: [$lo, $hi)")
        }
        for (i in lo until hi) {
            val r = i + uniform(hi - i)     // between i and hi-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified subarray in uniformly random order.
     *
     * @param a  the array to shuffle
     * @param lo the left endpoint (inclusive)
     * @param hi the right endpoint (exclusive)
     * @throws IllegalArgumentException  if `a` is `null`
     * @throws IndexOutOfBoundsException unless `(0 <= lo) && (lo < hi) && (hi <= a.length)`
     */
    fun shuffle(a: IntArray?, lo: Int, hi: Int) {
        if (a == null) throw IllegalArgumentException("argument array is null")
        if (lo < 0 || lo >= hi || hi > a.size) {
            throw IndexOutOfBoundsException("invalid subarray range: [$lo, $hi)")
        }
        for (i in lo until hi) {
            val r = i + uniform(hi - i)     // between i and hi-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Returns a uniformly random permutation of *n* elements
     *
     * @param n number of elements
     * @return an array of length `n` that is a uniformly random permutation
     * of `0`, `1`, ..., `n-1`
     * @throws IllegalArgumentException if `n` is negative
     */
    fun permutation(n: Int): IntArray {
        if (n < 0) throw IllegalArgumentException("argument is negative")
        val perm = IntArray(n)
        for (i in 0 until n)
            perm[i] = i
        shuffle(perm)
        return perm
    }

    /**
     * Returns a uniformly random permutation of *k* of *n* elements
     *
     * @param n number of elements
     * @param k number of elements to select
     * @return an array of length `k` that is a uniformly random permutation
     * of `k` of the elements from `0`, `1`, ..., `n-1`
     * @throws IllegalArgumentException if `n` is negative
     * @throws IllegalArgumentException unless `0 <= k <= n`
     */
    fun permutation(n: Int, k: Int): IntArray {
        if (n < 0) throw IllegalArgumentException("argument is negative")
        if (k < 0 || k > n) throw IllegalArgumentException("k must be between 0 and n")
        val perm = IntArray(k)
        for (i in 0 until k) {
            val r = uniform(i + 1)    // between 0 and i
            perm[i] = perm[r]
            perm[r] = i
        }
        for (i in k until n) {
            val r = uniform(i + 1)    // between 0 and i
            if (r < k) perm[r] = i
        }
        return perm
    }

    fun smallSleep(): Int {
        return nextGaussian(150, 250, 50)
    }

    fun medSleep(): Int {
        return nextGaussian(750, 1500, 250)
    }

    fun bigSleep(): Int {
        return nextGaussian(2000, 5000, 1500)
    }
}// don't instantiate
/**
 * Returns a random boolean from a Bernoulli distribution with success
 * probability 1/2.
 *
 * @return `true` with probability 1/2 and
 * `false` with probability 1/2
 */
/**
 * Returns a random real number from the standard Pareto distribution.
 *
 * @return a random real number from the standard Pareto distribution
 */