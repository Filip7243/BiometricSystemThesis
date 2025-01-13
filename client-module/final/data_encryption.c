#include "data_encryption.h"
#include <openssl/evp.h>
#include <openssl/rand.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <openssl/sha.h>

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

    // Debug: Print IV and key
    printf("IV being used: ");
    for (int i = 0; i < AES_BLOCK_SIZE; i++)
    {
        printf("%02x", iv[i]);
    }
    printf("\n");

    printf("Key being used (SHA-256 derived): ");
    for (int i = 0; i < 16; i++)
    {
        printf("%02x", key[i]);
    }
    printf("\n");

    // Zapisz IV na początku pliku
    fwrite(iv, 1, AES_BLOCK_SIZE, output_file);

    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    if (!ctx)
    {
        perror("Failed to create cipher context");
        fclose(input_file);
        fclose(output_file);
        return -1;
    }

    // Explicit set padding
    EVP_CIPHER_CTX_set_padding(ctx, 1);

    // Inicjalizacja szyfrowania
    if (EVP_EncryptInit_ex(ctx, EVP_aes_128_cbc(), NULL, key, iv) != 1)
    {
        perror("Failed to initialize encryption");
        EVP_CIPHER_CTX_free(ctx);
        fclose(input_file);
        fclose(output_file);
        return -1;
    }

    unsigned char input_buffer[1024];
    unsigned char output_buffer[1024 + EVP_MAX_BLOCK_LENGTH];
    int input_len, output_len;
    long total_input_len = 0;
    long total_output_len = AES_BLOCK_SIZE;

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

    printf("Input file size: %ld bytes\n", total_input_len);
    printf("Output file size (including IV): %ld bytes\n", total_output_len);

    EVP_CIPHER_CTX_free(ctx);
    fclose(input_file);
    fclose(output_file);

    return 0;
}

void generate_iv(unsigned char *iv)
{
    RAND_bytes(iv, AES_BLOCK_SIZE);
}