package org.arabicchemistry.lab

import java.math.BigInteger

/** Atom conservation for supplied neutral formulas. Does not predict chemical feasibility. */
object EquationBalancer {
    data class Balanced(val reactants: List<String>, val products: List<String>, val coefficients: List<Int>) {
        fun formatted(): String {
            val terms = (reactants + products).mapIndexed { i, f -> if (coefficients[i] == 1) f else "${coefficients[i]} $f" }
            return terms.take(reactants.size).joinToString(" + ") + " → " + terms.drop(reactants.size).joinToString(" + ")
        }
    }
    private data class Q(val n: BigInteger, val d: BigInteger = BigInteger.ONE) {
        fun norm(): Q {
            require(d.signum() != 0 && n.bitLength() <= 2048 && d.bitLength() <= 2048)
            val g = n.gcd(d); val sign = if (d.signum() < 0) -BigInteger.ONE else BigInteger.ONE
            return Q(n/g*sign, d/g*sign)
        }
        operator fun plus(b: Q) = Q(n*b.d+b.n*d,d*b.d).norm()
        operator fun unaryMinus() = Q(-n,d)
        operator fun minus(b: Q) = this + -b
        operator fun times(b: Q) = Q(n*b.n,d*b.d).norm()
        operator fun div(b: Q) = Q(n*b.d,d*b.n).norm()
        fun zero() = n.signum() == 0
    }
    fun atoms(formula: String, known: Set<String>): Map<String, Int> {
        require(formula.isNotEmpty() && formula.length <= 100)
        var position = 0
        fun count(): Int {
            val start = position
            while(position < formula.length && formula[position] in '0'..'9') position++
            if(start == position) return 1
            val s = formula.substring(start,position)
            require(!s.startsWith('0'))
            return s.toIntOrNull()?.also { require(it in 1..99) } ?: throw IllegalArgumentException("count")
        }
        fun group(depth: Int): Map<String,Int> {
            require(depth <= 4)
            val result = mutableMapOf<String,Int>()
            while(position < formula.length && formula[position] != ')') {
                val part = if(formula[position] == '(') {
                    position++; val nested = group(depth+1)
                    require(position < formula.length && formula[position] == ')'); position++; nested
                } else {
                    require(formula[position] in 'A'..'Z')
                    val start=position++
                    if(position < formula.length && formula[position] in 'a'..'z') position++
                    val symbol=formula.substring(start,position)
                    require(symbol in known); mapOf(symbol to 1)
                }
                val multiplier=count()
                part.forEach { (symbol,n) ->
                    val total=(result[symbol] ?: 0).toLong()+n.toLong()*multiplier
                    require(total <= 10000); result[symbol]=total.toInt()
                }
            }
            require(result.isNotEmpty()); return result
        }
        val result=group(0); require(position == formula.length); return result
    }
    fun balance(input: String, known: Set<String>): Balanced {
        require(input.length in 1..250)
        val normalized=input.filterNot { it.isWhitespace() }.map { c -> if(c in '₀'..'₉') '0'+(c-'₀') else c }.joinToString("").replace("→","->").replace("=","->")
        val sides=normalized.split("->"); require(sides.size == 2)
        val left=sides[0].split('+'); val right=sides[1].split('+')
        val formulas=left+right; require(formulas.size in 2..12 && formulas.none { it.isEmpty() })
        val parsed=formulas.map { atoms(it,known) }; val symbols=parsed.flatMap { it.keys }.distinct()
        val original=symbols.map { symbol -> parsed.mapIndexed { i, counts -> (counts[symbol] ?: 0) * if(i < left.size) 1 else -1 } }
        val matrix=original.map { row -> row.map { Q(BigInteger.valueOf(it.toLong())) }.toMutableList() }.toMutableList()
        val pivots=mutableListOf<Int>(); var row=0
        for(col in formulas.indices) {
            val pivot=(row until matrix.size).firstOrNull { !matrix[it][col].zero() } ?: continue
            val swap=matrix[row]; matrix[row]=matrix[pivot]; matrix[pivot]=swap
            val factor=matrix[row][col]
            for(j in col until formulas.size) matrix[row][j]=matrix[row][j]/factor
            for(i in matrix.indices) if(i != row && !matrix[i][col].zero()) {
                val f=matrix[i][col]
                for(j in col until formulas.size) matrix[i][j]=matrix[i][j]-f*matrix[row][j]
            }
            pivots.add(col); row++; if(row == matrix.size) break
        }
        val free=formulas.indices.filter { it !in pivots }; require(free.size == 1) { "non_unique" }
        val vector=MutableList(formulas.size) { Q(BigInteger.ZERO) }; vector[free[0]]=Q(BigInteger.ONE)
        pivots.forEachIndexed { r, col -> vector[col]=-matrix[r][free[0]] }
        val lcm=vector.fold(BigInteger.ONE) { a,q -> a/a.gcd(q.d)*q.d }
        var integers=vector.map { it.n*(lcm/it.d) }
        if(integers.first().signum() < 0) integers=integers.map { -it }
        require(integers.all { it.signum() > 0 }) { "no_positive_solution" }
        val gcd=integers.reduce { a,b -> a.gcd(b) }; integers=integers.map { it/gcd }
        require(integers.all { it <= BigInteger.valueOf(1000000) })
        original.forEach { counts -> require(counts.indices.fold(BigInteger.ZERO) { sum,i -> sum+BigInteger.valueOf(counts[i].toLong())*integers[i] }.signum() == 0) }
        return Balanced(left,right,integers.map { it.toInt() })
    }
}
