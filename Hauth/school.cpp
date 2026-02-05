#include <iostream>
using namespace std;

int main() {
    int algro;

    cout << "Please select an algorithm you want to test (1-6): " << endl;
    cin >> algro;

    switch (algro) {
        case 1: {
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
            cout << "This is algo 5" << endl;
            break;

        case 6:
            cout << "This is algo 6" << endl;
            break;

        default:
            cout << "Invalid selection! Please choose 1-6." << endl;
            break;
    } // THIS bracket closes the switch

    return 0;
}
