// Bubble Sort, Selection Sort, Insertion Sort, Quick Sort, Heap Sort.

object sorting extends App {
    var arr1 = Array(5, 3, 2, 1, 8, 6)
    var arr2 = Array(5, 6, 2, 1, 8, 3)
    var arr3 = Array(8, 3, 2, 1, 5, 6)
    var arr4 = Array(2, 3, 5, 1, 8, 6)
    var arr5 = Array(3, 1, 2, 5, 8, 6)

    def bubble_sort(arr: Array[Int]) : Unit = {
        val n  = arr.length
        for (i <- 0 until n - 1) {
            for (j <- 0 until n - 1 - i) {
                if (arr(j) > arr(j + 1)) {
                    val tmp = arr(j)
                    arr(j) = arr(j + 1)
                    arr(j + 1) = tmp
                }
            }
        }
    }

    def selection_sort(arr: Array[Int]): Unit = {
        val n = arr.length
        for (i <- 0 until n - 1) {
            var minIndex = i
            for (j <- i + 1 until n) {
                if (arr(j) < arr(minIndex)) {
                    minIndex = j
                }
            }
            if (minIndex != i) {
                val tmp = arr(i)
                arr(i) = arr(minIndex)
                arr(minIndex) = tmp
            }
        }
    }

    def insertion_sort(arr: Array[Int]): Unit = {
        val n = arr.length
        for (i <- 1 until n) {
            val key = arr(i)  
            var j = i - 1
            while (j >= 0 && arr(j) > key) {
                arr(j + 1) = arr(j)
                j -= 1
            }
            arr(j + 1) = key
        }
    }

    def quick_sort(arr: Array[Int], low: Int, high: Int): Unit = {
        if (low < high) {
            val pivotInd = partition(arr, low, high)
            quick_sort(arr, low, pivotInd - 1)
            quick_sort(arr, pivotInd + 1, high)
        }
    }

    // helper functin for finding the pivot index
    def partition(arr: Array[Int], low: Int, high: Int): Int = {
        val pivot = arr(high) // pivot is the last element in this case
        var i = low
        for (j <- low until high) {
            if (arr(j) <= pivot) {
                val tmp = arr(i)
                arr(i) = arr(j)
                arr(j) = tmp
                i += 1
            }
        }
        val tmp = arr(i)
        arr(i) = arr(high)
        arr(high) = tmp
        i
    }

    def heap_sort(arr: Array[Int]): Unit = {
        val n = arr.length
        for (i <- n / 2 - 1 to 0 by -1) {
            heapify(arr, n, i)
        }
        for (i <- n - 1 to 1 by -1) {
            val tmp = arr(0)
            arr(0) = arr(i)
            arr(i) = tmp
            heapify(arr, i, 0)
        }
    }

    // helper function for heap sort
    def heapify(arr: Array[Int], n: Int, i: Int): Unit = {
        var largest = i         // Initialize the largest as root
        val left = 2 * i + 1     // Left child
        val right = 2 * i + 2    // Right child

        if (left < n && arr(left) > arr(largest)) {
            largest = left
        }

        if (right < n && arr(right) > arr(largest)) {
            largest = right
        }
        // If the largest is not the root, swap and continue heapifying
        if (largest != i) {
            val tmp = arr(i)
            arr(i) = arr(largest)
            arr(largest) = tmp
            heapify(arr, n, largest)
        }
    }

    bubble_sort(arr1)
    selection_sort(arr2)
    insertion_sort(arr3)
    quick_sort(arr4, 0, arr4.length - 1)
    heap_sort(arr5)

    println(arr1.mkString(" -> "))
    println(arr2.mkString(" -> "))
    println(arr3.mkString(" -> "))
    println(arr4.mkString(" -> "))
    println(arr5.mkString(" -> "))

}