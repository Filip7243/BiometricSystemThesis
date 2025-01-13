#ifndef DATA_ENCRYPTION_H
#define DATA_ENCRYPTION_H

#include <openssl/evp.h>
#include <openssl/rand.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define AES_KEY_SIZE 16   // 128-bitowy klucz
#define AES_BLOCK_SIZE 16 // Rozmiar bloku AES

/**
 * Funkcja do generowania klucza za pomocą algorytmu SHA-256.
 *
 * @param secret    Wskaźnik na dane wejściowe (tajny tekst), z których generowany będzie klucz.
 * @param key       Wskaźnik na wynikowy klucz (256-bitowy), który będzie wygenerowany z danych wejściowych.
 *
 * @return Brak wartości zwrotnej - klucz jest zapisywany bezpośrednio w buforze 'key'.
 */
void generate_key_from_secret(const char *secret, unsigned char *key);

/**
 * Funkcja do szyfrowania tekstu za pomocą algorytmu AES-128-CBC.
 *
 * @param plaintext      Wskaźnik na dane wejściowe (tekst jawny).
 * @param plaintext_len  Długość tekstu jawnego.
 * @param key            Wskaźnik na klucz szyfrowania (128-bitowy).
 * @param iv             Wskaźnik na wektor inicjalizacyjny (IV).
 * @param ciphertext     Wskaźnik na zaszyfrowany wynik (tekst zaszyfrowany).
 *
 * @return Długość zaszyfrowanych danych w bajtach lub -1 w przypadku błędu.
 */
int encrypt_aes(const char *input_filename, const char *output_filename, const char *secret, unsigned char *iv);

/**
 * Funkcja generująca losowy wektor inicjalizacyjny (IV) o rozmiarze AES_BLOCK_SIZE.
 *
 * @param iv  Wskaźnik na bufor, w którym zostanie zapisany wektor IV.
 */
void generate_iv(unsigned char *iv);

#endif // DATA_ENCRYPTION_H