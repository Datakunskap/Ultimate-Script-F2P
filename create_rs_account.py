#!/usr/bin/env python2
import argparse
import requests
import sys
import time
import os
from random import randint

RUNESCAPE_REGISTER_URL = 'https://secure.runescape.com/m=account-creation/g=oldscape/create_account'
RUNESCAPE_RECAPTCHA_KEY = '6Lcsv3oUAAAAAGFhlKrkRb029OHio098bbeyi_Hv'
CAPTCHA_URL = 'http://2captcha.com/'
CAPTCHA_REQ_URL = CAPTCHA_URL + 'in.php'
CAPTCHA_RES_URL = CAPTCHA_URL + 'res.php'
CAPTCHA_API_KEY = '4935affd16c15fb4100e8813cdccfab6'


class WaitForCaptcha():
    def __init__(self):
        self.waited_for = 0

    def sleep(self, seconds):
        self.waited_for += seconds

        for i in range(0, seconds):
            if i is seconds - 1 and self.waited_for % 10 is 0:
                if self.waited_for > 30:
                    print(". (%ds)" % self.waited_for)
                else:
                    print('.')
            else:
                print('.',
                      sys.stdout.flush())
            time.sleep(1)


def register_account(email, password):
    print('''Registering account with:
    Email: %s
    Password: %s ''' % (email, password))

    response = requests.post(RUNESCAPE_REGISTER_URL, data={
        'theme': 'oldschool',
        'email1': email,
        'onlyOneEmail': 1,
        'password1': password,
        'onlyOnePassword': 1,
        'day': randint(1, 28),
        'month': randint(1, 12),
        'year': randint(1995, 2005),
        'agree_email': 1,
        'agree_email_third_party': 1,
        'g-recaptcha-response': solve_captcha(5),
        'create-submit': 'Play Now'
    })

    if response.status_code == requests.codes.ok:
        if 'Account Created' in response.text:
            print('Robots win again, account successfully registered\n\n')

            with open('C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\f2pAccounts.txt', 'a+') as f:
                f.write('%s\n' % email)
                f.close()

        else:
            print(response.text)
            # raise Exception('Jagex says no')
    else:
        print(response.text)
        # raise Exception('Jagex says no')


def solve_captcha(retries):
    print('Solving Captcha')
    waiting = True
    touched = False
    captcha_id = None

    response = requests.get(CAPTCHA_REQ_URL, params={
        'key': CAPTCHA_API_KEY,
        'method': 'userrecaptcha',
        'googlekey': RUNESCAPE_RECAPTCHA_KEY,
        'pageurl': RUNESCAPE_REGISTER_URL
    })

    if retries < 1:
        if response.status_code != requests.codes.ok:
            raise Exception('2Captcha says no')
        else:
            solve_captcha(retries - 1)
        raise Exception('Captcha request failed')

    if response.status_code != requests.codes.ok:
        solve_captcha(retries - 1)

    if '|' in response.text:
        _, captcha_id = response.text.split('|')
    else:
        solve_captcha(retries - 1)

    wait_for_captcha = WaitForCaptcha()

    print('Waiting for captcha (ID: %s) to be solved' % captcha_id)
    while waiting:
        wait_for_captcha.sleep(5 if touched else 15)

        touched = True

        solution_response = requests.get(CAPTCHA_RES_URL, params={
            'key': CAPTCHA_API_KEY,
            'action': 'get',
            'id': captcha_id
        })

        if solution_response.text not in ('CAPCHA_NOT_READY', 'CAPTCHA_NOT_READY'):
            print('\nCaptcha solved after %ds! (solution: %s)' % (wait_for_captcha.waited_for, solution_response.text))
            waiting = False
            _, captcha_solution = solution_response.text.split('|')
            return captcha_solution


if not len(sys.argv) > 1:
    print('You forgot to pass in any arguments! Run with -h/--help for more info')
    sys.exit()

parser = argparse.ArgumentParser(description='Create Runescape account(s)\n'
                                             'Pass new account details or path to a file with list of them',
                                 formatter_class=argparse.RawTextHelpFormatter)

single_acc_arg_group = parser.add_argument_group('Create a single account')

single_acc_arg_group.add_argument('-e', '--email', nargs=1,
                                  help='Email address to use for the new account')
single_acc_arg_group.add_argument('-p', '--password', nargs=1,
                                  help='Password')

acc_list_arg_group = parser.add_argument_group('Create accounts from a list')

acc_list_arg_group.add_argument('-l', '--list', nargs=1,
                                help='''Path to file with list of new account details
        Syntax within files should match:
        email:password''')

args = parser.parse_args()

if args.list:
    accounts_file = open(args.list[0])
    accounts = accounts_file.readlines()
    accounts_file.close()

    for account in accounts:
        email, password = account.rstrip().split(':')
        register_account(email, password)

elif args.email and args.password:
    register_account(args.email[0], args.password[0])

else:
    print('Not enough arguments! Run with -h/--help for more info')
