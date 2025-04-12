#include "data_encryption.h"
#include <openssl/evp.h>
#include <openssl/rand.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <openssl/sha.h>

// Generowanie klucz z podanego sekretu przy użyciu algorytmu SHA-256
void generate_key_from_secret(const char *secret, unsigned char *key)
{
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, secret, strlen(secret));
    SHA256_Final(hash, &sha256);

    // Kopiujemy tylko pierwsze 16 bajtów (tak jak w Javie)
    memcpy(key, hash, 16);
}

// Szyfrowanie pliku za pomocą algorytmu AES-128-CBC
int encrypt_aes(const char *input_filename, const char *output_filename, const char *secret, unsigned char *iv)
{
    // Generowanie klucza
    unsigned char key[16];
    generate_key_from_secret(secret, key);

    FILE *input_file = fopen(input_filename, "rb");
    if (!input_file)
    {
        perror("Unable to open input file");
        return -1;
    }

    FILE *output_file = fopen(output_filename, "wb");
    if (!output_file)
    {
        perror("Unable to open output file");
        fclose(input_file);
        return -1;
    }

    // Zapisz IV na początku pliku
    fwrite(iv, 1, AES_BLOCK_SIZE, output_file);

    // Utworzenie nowego kontekstu szyfrowania
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    if (!ctx)
    {
        perror("Failed to create cipher context");
        fclose(input_file);
        fclose(output_file);
        return -1;
    }

    // Ustawienie paddingu (dodawanie wypełnienia)
    EVP_CIPHER_CTX_set_padding(ctx, 1);

    // Inicjalizacja szyfrowania AES-128-CBC
    if (EVP_EncryptInit_ex(ctx, EVP_aes_128_cbc(), NULL, key, iv) != 1)
    {
        perror("Failed to initialize encryption");
        EVP_CIPHER_CTX_free(ctx);
        fclose(input_file);
        fclose(output_file);
        return -1;
    }

    // Bufory na dane wejściowe i wyjściowe
    unsigned char input_buffer[1024];
    unsigned char output_buffer[1024 + EVP_MAX_BLOCK_LENGTH]; // Bufor na dane wyjściowe (z uwzględnieniem paddingu)
    int input_len, output_len;
    long total_input_len = 0;               // Liczba odczytanych bajtów z pliku wejściowego
    long total_output_len = AES_BLOCK_SIZE; // Rozmiar bloku danych wyjściowych

    // Przetwarzanie pliku wejściowego w blokach
    while ((input_len = fread(input_buffer, 1, sizeof(input_buffer), input_file)) > 0)
    {
        if (EVP_EncryptUpdate(ctx, output_buffer, &output_len, input_buffer, input_len) != 1)
        {
            perror("Encryption update failed");
            EVP_CIPHER_CTX_free(ctx);
            fclose(input_file);
            fclose(output_file);
            return -1;
        }
        fwrite(output_buffer, 1, output_len, output_file);
        total_input_len += input_len;
        total_output_len += output_len;
    }

    // Finalizacja szyfrowania (przetwarzanie pozostałych bajtów)
    if (EVP_EncryptFinal_ex(ctx, output_buffer, &output_len) != 1)
    {
        perror("Encryption finalization failed");
        EVP_CIPHER_CTX_free(ctx);
        fclose(input_file);
        fclose(output_file);
        return -1;
    }
    fwrite(output_buffer, 1, output_len, output_file);
    total_output_len += output_len;

    // Czyszczenie kontekstu szyfrowania
    EVP_CIPHER_CTX_free(ctx);
    fclose(input_file);
    fclose(output_file);

    return 0;
}

// Funkcja generująca losowy wektor inicjalizacyjny (IV)
void generate_iv(unsigned char *iv)
{
    RAND_bytes(iv, AES_BLOCK_SIZE);
}