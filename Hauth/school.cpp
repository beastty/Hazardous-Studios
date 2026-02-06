#include <iostream>
#include <string>
using namespace std;
void number();
int main() {
    int algro;

    cout << "Please select an algorithm you want to test (1-7): " << endl;
    cin >> algro;

    switch (algro) {
        case 1: {
          number();
            break;
        }

        case 2: {
            int count;
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
            break;
        }


        case 3: {
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
            break;

        case 4: {
    int num;
            cout << "Please pick a number to repeat: " << endl;
            cin >> num;

            for (int i=1; i<=num; i++) {
            }
            cout << num;
        }
            break;

        case 5:
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

            break;

        case 6: {
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

            break;

        case 7: {
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
break;
        default:
            cout << "Invalid selection! Please choose 1-7." << endl;
            break;
    } // THIS bracket closes the switch


    return 0;
}



void number() {
    int a, b;
    cout << "Please enter your first number: " << endl;
    cin >> a;
    cout << "Please enter your second number: " << endl;
    cin >> b;

    bool isEqual = (a == b);
    bool isSmaller = (a < b);

    if (isEqual) {
        cout << "The numbers are equal to each other!" << endl;
    } else if (isSmaller) {
        cout << "Your first number is smaller!" << endl;
    } else {
        cout << "The first number is larger!" << endl;
    }
}
