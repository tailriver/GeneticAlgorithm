package net.tailriver.science.ga.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import net.tailriver.science.ga.Individual;

/**
 * Solves Michalewicz sample with gnuplot animation.
 * 
 * @author tailriver
 */
public class MichalewiczPlot extends Michalewicz {
	Process gnuplot;
	InputStream cin;
	InputStream cerr;
	PrintWriter cout;
	Individual best;
	int generation;

	public static void main(String... args) {
		MichalewiczPlot mi = new MichalewiczPlot();
		for (mi.generation = 0; mi.generation < 1000; mi.generation++) {
			mi.ga.cross(0.25, 1);
			mi.ga.mutate(0.01);

			Individual generationTop = mi.ga.getRankAt(1);
			if (generationTop.isGreaterThan(mi.best)) {
				mi.best = generationTop;
				System.out.println(">> " + mi.generation);
				mi.best.print();
				System.out.println();
			}
			mi.plot();
			mi.ga.select();
		}
	}

	public void plot() {
		long sleep = 500;
		if (generation > 30 && best != null && best.getFitness() > 3.6) {
			cout.println("set xrange [1.6:1.9]");
			cout.println("set yrange [3:4]");
		}
		if (generation > 30) {
			sleep = 100;
		}
		cout.println("set title 'generation #" + generation + "'");
		cout.println("plot f(x) with line, '-' with point pt 5 ps 3");
		for (int i = 1; i <= 50; i++) {
			Individual one = ga.getRankAt(i);
			cout.println(one.getGenoTypeDouble(0, -1, 2) + " "
					+ one.getFitness());
		}
		cout.println("e");
		cout.flush();

		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public MichalewiczPlot() {
		ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/gnuplot");
		try {
			gnuplot = pb.start();
			cin = gnuplot.getInputStream();
			cerr = gnuplot.getErrorStream();
			cout = new PrintWriter(gnuplot.getOutputStream());
			cout.println("set xrange [-1:2]");
			cout.println("set samples 500");
			cout.println("set key left top");
			cout.println("f(x) = x * sin(10*pi*x) + 2");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (cin != null)
			cin.close();
		if (cerr != null)
			cerr.close();
		if (cout != null)
			cout.close();
		if (gnuplot != null)
			gnuplot.destroy();
		super.finalize();
	}
}
