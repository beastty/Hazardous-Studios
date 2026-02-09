#include <iostream>
#include <string>
using namespace std;

// Starts declaring all the functions from the bottom of the code
void number();
void triangle();
void sum();
void repeat();
void fuzz();
void silly();
void sentence();
// End of declaring functions


// Start of function main
int main() {
    int algro;

    // asks and gets the user input for what algro they would like to test
    cout << "Please select an algorithm you want to test (1-7): " << endl;
    cin >> algro;


    // algro switcher( swiches what function to test )
    switch (algro) {
        case 1: {
          number();
            break;
        }

        case 2: {
           triangle();
            break;
        }


        case 3: {
        sum();
        }
            break;

        case 4: {
    repeat();
        }
            break;

        case 5: {
            fuzz;
        }
            break;

        case 6: {
            silly();

        }

            break;

        case 7: {
            sentence();

        }
break;
            // default if theey select something not in the switch statements to make them re select
        default:
            cout << "Invalid selection! Please choose 1-7." << endl;
            break;
    } // THIS bracket closes the switch


    return 0;
} // End of function main


// Start of the  number function
void number() {
    int a, b;
    // asks the users for their first number and second number then it sets the int a / b var to the number the user sets
    cout << "Please enter your first number: " << endl;
    cin >> a;
    cout << "Please enter your second number: " << endl;
    cin >> b;


    bool isEqual = (a == b);
    bool isSmaller = (a < b);

    if (isEqual) { // if the 2 numbers are equal
        cout << "The numbers are equal to each other!" << endl;
    } else if (isSmaller) { // if the first number is smaller then the second number
        cout << "Your first number is smaller!" << endl;
    } else { // if the first number is larger then the second number
        cout << "The first number is larger!" << endl;
    }
}

void triangle() {
    int count;
    // asks the user how many triangler numbers they want then the input is what the console sets it to
    cout << "Enter how many triangular numbers: ";
    cin >> count;

    int n = 1;

    while (n <= count) {
        int triangleNum = n * (n + 1) / 2;
        cout << "\nTriangle #" << n << " is: " << triangleNum << endl;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= i; j++) {
                cout << "* ";
            }
            cout << endl;
        }
        n++;
    }
}

void sum() {
    int num;
    int sum = 0;
    float ave;

    cout << "Enter your first numbers: ";
    for (int i = 0; i < 10; i++) {
        cin >> num;
        sum += num;

        ave = sum/10.00;
    }
    cout << "The sum of all your numbers is: " << sum << endl;
    cout << "The average number is: " << ave << endl;
}

void repeat() {
    int num;
    cout << "Please pick a number to repeat: " << endl;
    cin >> num;

    for (int i=1; i<=num; i++) {
    }
    cout << num;
}

void fuzz() {
    int num;
    cout << "Please pick a number: " << endl;
    cin >> num;

    for (int i =1; i <= num; i++) {
        if (i%3 == 0 && i % 5 == 0) {
            cout << "FizzBuzz";
        } else if (i%3 == 0) {
            cout << "Fizz";
        } else if (i % 5 == 0) {

            cout << "Buzz";
        } else {
            cout << i << " ";
        }
    }
}

void silly() {
    string response;
    cout << "Do you love fixing error's in your 10 thousand line code without any real explanation of the error? (yes/no): ";
    getline(cin, response);

    if (response.compare("yes") ==0 ) {
        cout << "You are insane or just really love fixing code that breaks!" << endl;

    } else if (response.compare("no") == 0) {
        cout << "Honestly who does like fixing code?" << endl;
    } else {
        cout << "Please say yes or no to the question :( " << endl;
    }
}

void sentence() {
    string s1, s2;
    cout << "\n Enter first sentence: ";
    cin.clear();
    fflush(stdin);
    getline(cin, s1);
    cout << "Enter second sentence: ";
    getline(cin, s2);

    string combined = s1 + " " + s2;
    cout << "\m Joined sentences: " << combined << endl;
}
