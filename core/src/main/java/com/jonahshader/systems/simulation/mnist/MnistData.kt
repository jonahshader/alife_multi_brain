package com.jonahshader.systems.simulation.mnist

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.FileInputStream

object MnistData {
    private var train = listOf<Pair<IntArray, Int>>()
    private var test = listOf<Pair<IntArray, Int>>()

    // parts from https://github.com/turkdogan/mnist-data-reader/blob/master/MnistDataReader.java
    fun load() {
        if (train.isNotEmpty()) return
        println("Loading MNIST")
        train = loadSet("data/mnist/train-images.idx3-ubyte", "data/mnist/train-labels.idx1-ubyte")
        test = loadSet("data/mnist/t10k-images.idx3-ubyte", "data/mnist/t10k-labels.idx1-ubyte")
        println("Done loading MNIST")
    }

    fun getRandomTrainingData() = train.random()

    //"data/mnist/train-images-idx3-ubyte.gz"
    private fun loadSet(dataFilePath: String, labelFilePath: String) : List<Pair<IntArray, Int>> {
        val dataInputStream = DataInputStream(BufferedInputStream(FileInputStream(dataFilePath)))
        val magicNumber = dataInputStream.readInt()
        val numberOfItems = dataInputStream.readInt()
        val nRows = dataInputStream.readInt()
        val nCols = dataInputStream.readInt()

        println("magic number is $magicNumber")
        println("number of items is $numberOfItems")
        println("number of rows is: $nRows")
        println("number of cols is: $nCols")

        val labelInputStream = DataInputStream(BufferedInputStream(FileInputStream(labelFilePath)))
        val labelMagicNumber = labelInputStream.readInt()
        val numberOfLabels = labelInputStream.readInt()

        println("labels magic number is: $labelMagicNumber")
        println("number of labels is: $numberOfLabels")

        val outputList = mutableListOf<Pair<IntArray, Int>>()

        for (i in 0 until numberOfItems) {
            val input = IntArray(nRows * nCols)
            val output = labelInputStream.readUnsignedByte()
            for (j in 0 until nRows * nCols) {
                input[j] = dataInputStream.readUnsignedByte()
            }

            outputList += Pair(input, output)
        }

        dataInputStream.close()
        labelInputStream.close()
        return outputList
    }
}

