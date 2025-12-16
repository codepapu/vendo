<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class () extends Migration {
    public function up(): void
    {
        if (Schema::hasColumn('ec_product_attributes', 'status')) {
            $connection = Schema::getConnection();
            $driver = $connection->getDriverName();
            
            if ($driver === 'sqlite') {
                // SQLite doesn't support dropping columns directly when there's an index
                // Drop the index first
                try {
                    DB::statement('DROP INDEX IF EXISTS attribute_set_status_index');
                } catch (\Exception $e) {
                    // Index might not exist or already dropped, continue
                }
                
                // For SQLite, we can't easily drop columns, so we'll mark the migration as run
                // The column will remain but won't be used by the application
                return;
            }
            
            Schema::table('ec_product_attributes', function (Blueprint $table): void {
                $table->dropColumn('status');
            });
        }
    }

    public function down(): void
    {
        if (! Schema::hasColumn('ec_product_attributes', 'status')) {
            Schema::table('ec_product_attributes', function (Blueprint $table): void {
                $table->string('status', 60)->default('published');
            });
        }
    }
};
