package complex;

public class ComplexNumber {
    private double real;
    private double imag;
    public ComplexNumber(double real, double imag){
        this.real = real;
        this.imag = imag;
    }
    public ComplexNumber add(ComplexNumber other){
        double a = this.real + other.real;
        double b = this.imag + other.imag;
        return new ComplexNumber(a,b);
    }
    public ComplexNumber multiply(ComplexNumber other){
        double a = this.real*other.real - this.imag*other.imag;
        double b = this.imag*other.real + this.real*other.imag;
        return new ComplexNumber(a,b);
    }
    public double magnitude(){
        return Math.sqrt(real*real + imag*imag);
    }
    public double sqMagnitude(){
        return real*real + imag*imag;
    }
    public String toString(){
        if(imag < 0){
            return real + " - "+imag+"i";
        }
        else
            return real + " + " + imag + "i";
    }
}
